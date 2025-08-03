package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.common.model.enums.ReferenceType;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Priority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {

    private final Map<Long, TaskManagement> taskStore = new ConcurrentHashMap<>();
    private final Map<Long, TaskComment> commentStore = new ConcurrentHashMap<>();
    private final Map<Long, TaskActivity> activityStore = new ConcurrentHashMap<>();
    private final AtomicLong taskIdCounter = new AtomicLong(0);
    private final AtomicLong commentIdCounter = new AtomicLong(0);
    private final AtomicLong activityIdCounter = new AtomicLong(0);

    public InMemoryTaskRepository() {
        // Seed data
        createSeedTask(101L, ReferenceType.ORDER, Task.CREATE_INVOICE, 1L, TaskStatus.ASSIGNED, Priority.HIGH);
        createSeedTask(101L, ReferenceType.ORDER, Task.ARRANGE_PICKUP, 1L, TaskStatus.COMPLETED, Priority.HIGH);
        createSeedTask(102L, ReferenceType.ORDER, Task.CREATE_INVOICE, 2L, TaskStatus.ASSIGNED, Priority.MEDIUM);
        createSeedTask(201L, ReferenceType.ENTITY, Task.ASSIGN_CUSTOMER_TO_SALES_PERSON, 2L, TaskStatus.ASSIGNED, Priority.LOW);
        createSeedTask(201L, ReferenceType.ENTITY, Task.ASSIGN_CUSTOMER_TO_SALES_PERSON, 3L, TaskStatus.ASSIGNED, Priority.LOW); // Duplicate for Bug #1
        createSeedTask(103L, ReferenceType.ORDER, Task.COLLECT_PAYMENT, 1L, TaskStatus.CANCELLED, Priority.MEDIUM); // For Bug #2
    }

    private void createSeedTask(Long refId, ReferenceType refType, Task task, Long assigneeId, TaskStatus status, Priority priority) {
        long newId = taskIdCounter.incrementAndGet();
        long currentTime = System.currentTimeMillis();
        TaskManagement newTask = new TaskManagement();
        newTask.setId(newId);
        newTask.setReferenceId(refId);
        newTask.setReferenceType(refType);
        newTask.setTask(task);
        newTask.setAssigneeId(assigneeId);
        newTask.setStatus(status);
        newTask.setPriority(priority);
        newTask.setDescription("This is a seed task.");
        newTask.setTaskDeadlineTime(currentTime + 86400000); // 1 day from now
        newTask.setCreatedTime(currentTime);
        if (status == TaskStatus.STARTED || status == TaskStatus.COMPLETED) {
            newTask.setStartedTime(currentTime - 3600000); // 1 hour ago
        }
        taskStore.put(newId, newTask);
        
        // Add creation activity
        addActivity(newId, null, "TASK_CREATED", "Task created with status: " + status);
    }

    @Override
    public Optional<TaskManagement> findById(Long id) {
        TaskManagement task = taskStore.get(id);
        if (task != null) {
            // Load comments and activities
            task.setComments(getCommentsForTask(id));
            task.setActivities(getActivitiesForTask(id));
        }
        return Optional.ofNullable(task);
    }

    @Override
    public TaskManagement save(TaskManagement task) {
        if (task.getId() == null) {
            task.setId(taskIdCounter.incrementAndGet());
            task.setCreatedTime(System.currentTimeMillis());
            addActivity(task.getId(), null, "TASK_CREATED", "Task created");
        }
        taskStore.put(task.getId(), task);
        return task;
    }

    @Override
    public List<TaskManagement> findAll() {
        return taskStore.values().stream()
                .map(this::loadTaskWithCommentsAndActivities)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskManagement> findByReferenceIdAndReferenceType(Long referenceId, ReferenceType referenceType) {
        return taskStore.values().stream()
                .filter(task -> task.getReferenceId().equals(referenceId) && task.getReferenceType().equals(referenceType))
                .map(this::loadTaskWithCommentsAndActivities)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskManagement> findByAssigneeIdIn(List<Long> assigneeIds) {
        return taskStore.values().stream()
                .filter(task -> assigneeIds.contains(task.getAssigneeId()))
                .map(this::loadTaskWithCommentsAndActivities)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskManagement> findByPriority(Priority priority) {
        return taskStore.values().stream()
                .filter(task -> task.getPriority() == priority)
                .map(this::loadTaskWithCommentsAndActivities)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskManagement> findByStatus(TaskStatus status) {
        return taskStore.values().stream()
                .filter(task -> task.getStatus() == status)
                .map(this::loadTaskWithCommentsAndActivities)
                .collect(Collectors.toList());
    }

    public TaskComment saveComment(TaskComment comment) {
        if (comment.getId() == null) {
            comment.setId(commentIdCounter.incrementAndGet());
            comment.setTimestamp(System.currentTimeMillis());
        }
        commentStore.put(comment.getId(), comment);
        
        // Add activity for comment
        addActivity(comment.getTaskId(), comment.getUserId(), "COMMENT_ADDED", "Comment added");
        
        return comment;
    }

    public TaskActivity addActivity(Long taskId, Long userId, String activityType, String description) {
        TaskActivity activity = new TaskActivity();
        activity.setId(activityIdCounter.incrementAndGet());
        activity.setTaskId(taskId);
        activity.setUserId(userId);
        activity.setActivityType(activityType);
        activity.setDescription(description);
        activity.setTimestamp(System.currentTimeMillis());
        activityStore.put(activity.getId(), activity);
        return activity;
    }

    private List<TaskComment> getCommentsForTask(Long taskId) {
        return commentStore.values().stream()
                .filter(comment -> comment.getTaskId().equals(taskId))
                .sorted((c1, c2) -> Long.compare(c1.getTimestamp(), c2.getTimestamp()))
                .collect(Collectors.toList());
    }

    private List<TaskActivity> getActivitiesForTask(Long taskId) {
        return activityStore.values().stream()
                .filter(activity -> activity.getTaskId().equals(taskId))
                .sorted((a1, a2) -> Long.compare(a1.getTimestamp(), a2.getTimestamp()))
                .collect(Collectors.toList());
    }

    private TaskManagement loadTaskWithCommentsAndActivities(TaskManagement task) {
        task.setComments(getCommentsForTask(task.getId()));
        task.setActivities(getActivitiesForTask(task.getId()));
        return task;
    }
}
