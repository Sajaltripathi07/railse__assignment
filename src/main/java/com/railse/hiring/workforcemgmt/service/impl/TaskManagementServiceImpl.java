package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.InMemoryTaskRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;
    private final InMemoryTaskRepository inMemoryTaskRepository;
    private final ITaskManagementMapper taskMapper;

    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.inMemoryTaskRepository = (InMemoryTaskRepository) taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            updateStatusIfChanged(task, item.getTaskStatus());
            updateDescriptionIfChanged(task, item.getDescription());

            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());
        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
                request.getReferenceId(), request.getReferenceType());

        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksOfType = existingTasks.stream()
                    .filter(t -> t.getTask() == taskType && t.getStatus() != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());

            if (!tasksOfType.isEmpty()) {
                reassignTask(tasksOfType.get(0), request.getAssigneeId());

                for (int i = 1; i < tasksOfType.size(); i++) {
                    cancelTask(tasksOfType.get(i));
                }
            } else {
                TaskManagement newTask = new TaskManagement();
                newTask.setReferenceId(request.getReferenceId());
                newTask.setReferenceType(request.getReferenceType());
                newTask.setTask(taskType);
                newTask.setAssigneeId(request.getAssigneeId());
                newTask.setStatus(TaskStatus.ASSIGNED);
                newTask.setDescription("Task assigned by reference");
                taskRepository.save(newTask);
            }
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());
        Long start = request.getStartDate();
        Long end = request.getEndDate();

        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> task.getStatus() != TaskStatus.CANCELLED)
                .filter(task -> isTaskInSmartRange(task, start, end))
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public TaskManagementDto updateTaskPriority(UpdatePriorityRequest request) {
        TaskManagement task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        Priority oldPriority = task.getPriority();
        task.setPriority(request.getPriority());
        taskRepository.save(task);

        logActivity(task.getId(), "PRIORITY_CHANGED", String.format("Priority changed from %s to %s", oldPriority, request.getPriority()));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> findTasksByPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    @Override
    public TaskManagementDto addComment(AddCommentRequest request) {
        TaskManagement task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + request.getTaskId()));

        TaskComment comment = new TaskComment();
        comment.setTaskId(request.getTaskId());
        comment.setUserId(request.getUserId());
        comment.setComment(request.getComment());
        inMemoryTaskRepository.saveComment(comment);

        return findTaskById(request.getTaskId());
    }

    // ===================== helper methods =====================

    private void logActivity(Long taskId, String eventType, String description) {
        inMemoryTaskRepository.addActivity(taskId, null, eventType, description);
    }

    private void reassignTask(TaskManagement task, Long newAssigneeId) {
        Long oldAssigneeId = task.getAssigneeId();
        task.setAssigneeId(newAssigneeId);
        taskRepository.save(task);
        logActivity(task.getId(), "TASK_REASSIGNED", String.format("Task reassigned from assignee %d to %d", oldAssigneeId, newAssigneeId));
    }

    private void cancelTask(TaskManagement task) {
        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
        logActivity(task.getId(), "TASK_CANCELLED", "Task cancelled due to reassignment");
    }

    private void updateStatusIfChanged(TaskManagement task, TaskStatus newStatus) {
        if (newStatus != null && newStatus != task.getStatus()) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(newStatus);

            if (newStatus == TaskStatus.STARTED && task.getStartedTime() == null) {
                task.setStartedTime(System.currentTimeMillis());
            }

            logActivity(task.getId(), "STATUS_CHANGED", String.format("Status changed from %s to %s", oldStatus, newStatus));
        }
    }

    private void updateDescriptionIfChanged(TaskManagement task, String newDescription) {
        if (newDescription != null && !newDescription.equals(task.getDescription())) {
            task.setDescription(newDescription);
            logActivity(task.getId(), "DESCRIPTION_UPDATED", "Task description updated");
        }
    }

    private boolean isTaskInSmartRange(TaskManagement task, long start, long end) {
        Long createdTime = task.getCreatedTime();
        if (createdTime == null) return false;

        boolean inRange = createdTime >= start && createdTime <= end;
        boolean pendingBefore = (task.getStatus() != TaskStatus.COMPLETED && createdTime < start);
        return inRange || pendingBefore;
    }
}
