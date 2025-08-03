package com.railse.hiring.workforcemgmt.mapper;

import com.railse.hiring.workforcemgmt.dto.TaskActivityDto;
import com.railse.hiring.workforcemgmt.dto.TaskCommentDto;
import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-03T11:14:37+0530",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class ITaskManagementMapperImpl implements ITaskManagementMapper {

    @Override
    public TaskManagementDto modelToDto(TaskManagement model) {
        if ( model == null ) {
            return null;
        }

        TaskManagementDto taskManagementDto = new TaskManagementDto();

        taskManagementDto.setActivities( taskActivityListToTaskActivityDtoList( model.getActivities() ) );
        taskManagementDto.setAssigneeId( model.getAssigneeId() );
        taskManagementDto.setComments( taskCommentListToTaskCommentDtoList( model.getComments() ) );
        taskManagementDto.setCreatedTime( model.getCreatedTime() );
        taskManagementDto.setDescription( model.getDescription() );
        taskManagementDto.setId( model.getId() );
        taskManagementDto.setPriority( model.getPriority() );
        taskManagementDto.setReferenceId( model.getReferenceId() );
        taskManagementDto.setReferenceType( model.getReferenceType() );
        taskManagementDto.setStartedTime( model.getStartedTime() );
        taskManagementDto.setStatus( model.getStatus() );
        taskManagementDto.setTask( model.getTask() );
        taskManagementDto.setTaskDeadlineTime( model.getTaskDeadlineTime() );

        return taskManagementDto;
    }

    @Override
    public TaskManagement dtoToModel(TaskManagementDto dto) {
        if ( dto == null ) {
            return null;
        }

        TaskManagement taskManagement = new TaskManagement();

        taskManagement.setActivities( taskActivityDtoListToTaskActivityList( dto.getActivities() ) );
        taskManagement.setAssigneeId( dto.getAssigneeId() );
        taskManagement.setComments( taskCommentDtoListToTaskCommentList( dto.getComments() ) );
        taskManagement.setCreatedTime( dto.getCreatedTime() );
        taskManagement.setDescription( dto.getDescription() );
        taskManagement.setId( dto.getId() );
        taskManagement.setPriority( dto.getPriority() );
        taskManagement.setReferenceId( dto.getReferenceId() );
        taskManagement.setReferenceType( dto.getReferenceType() );
        taskManagement.setStartedTime( dto.getStartedTime() );
        taskManagement.setStatus( dto.getStatus() );
        taskManagement.setTask( dto.getTask() );
        taskManagement.setTaskDeadlineTime( dto.getTaskDeadlineTime() );

        return taskManagement;
    }

    @Override
    public List<TaskManagementDto> modelListToDtoList(List<TaskManagement> models) {
        if ( models == null ) {
            return null;
        }

        List<TaskManagementDto> list = new ArrayList<TaskManagementDto>( models.size() );
        for ( TaskManagement taskManagement : models ) {
            list.add( modelToDto( taskManagement ) );
        }

        return list;
    }

    protected TaskActivityDto taskActivityToTaskActivityDto(TaskActivity taskActivity) {
        if ( taskActivity == null ) {
            return null;
        }

        TaskActivityDto taskActivityDto = new TaskActivityDto();

        taskActivityDto.setActivityType( taskActivity.getActivityType() );
        taskActivityDto.setDescription( taskActivity.getDescription() );
        taskActivityDto.setId( taskActivity.getId() );
        taskActivityDto.setTaskId( taskActivity.getTaskId() );
        taskActivityDto.setTimestamp( taskActivity.getTimestamp() );
        taskActivityDto.setUserId( taskActivity.getUserId() );

        return taskActivityDto;
    }

    protected List<TaskActivityDto> taskActivityListToTaskActivityDtoList(List<TaskActivity> list) {
        if ( list == null ) {
            return null;
        }

        List<TaskActivityDto> list1 = new ArrayList<TaskActivityDto>( list.size() );
        for ( TaskActivity taskActivity : list ) {
            list1.add( taskActivityToTaskActivityDto( taskActivity ) );
        }

        return list1;
    }

    protected TaskCommentDto taskCommentToTaskCommentDto(TaskComment taskComment) {
        if ( taskComment == null ) {
            return null;
        }

        TaskCommentDto taskCommentDto = new TaskCommentDto();

        taskCommentDto.setComment( taskComment.getComment() );
        taskCommentDto.setId( taskComment.getId() );
        taskCommentDto.setTaskId( taskComment.getTaskId() );
        taskCommentDto.setTimestamp( taskComment.getTimestamp() );
        taskCommentDto.setUserId( taskComment.getUserId() );

        return taskCommentDto;
    }

    protected List<TaskCommentDto> taskCommentListToTaskCommentDtoList(List<TaskComment> list) {
        if ( list == null ) {
            return null;
        }

        List<TaskCommentDto> list1 = new ArrayList<TaskCommentDto>( list.size() );
        for ( TaskComment taskComment : list ) {
            list1.add( taskCommentToTaskCommentDto( taskComment ) );
        }

        return list1;
    }

    protected TaskActivity taskActivityDtoToTaskActivity(TaskActivityDto taskActivityDto) {
        if ( taskActivityDto == null ) {
            return null;
        }

        TaskActivity taskActivity = new TaskActivity();

        taskActivity.setActivityType( taskActivityDto.getActivityType() );
        taskActivity.setDescription( taskActivityDto.getDescription() );
        taskActivity.setId( taskActivityDto.getId() );
        taskActivity.setTaskId( taskActivityDto.getTaskId() );
        taskActivity.setTimestamp( taskActivityDto.getTimestamp() );
        taskActivity.setUserId( taskActivityDto.getUserId() );

        return taskActivity;
    }

    protected List<TaskActivity> taskActivityDtoListToTaskActivityList(List<TaskActivityDto> list) {
        if ( list == null ) {
            return null;
        }

        List<TaskActivity> list1 = new ArrayList<TaskActivity>( list.size() );
        for ( TaskActivityDto taskActivityDto : list ) {
            list1.add( taskActivityDtoToTaskActivity( taskActivityDto ) );
        }

        return list1;
    }

    protected TaskComment taskCommentDtoToTaskComment(TaskCommentDto taskCommentDto) {
        if ( taskCommentDto == null ) {
            return null;
        }

        TaskComment taskComment = new TaskComment();

        taskComment.setComment( taskCommentDto.getComment() );
        taskComment.setId( taskCommentDto.getId() );
        taskComment.setTaskId( taskCommentDto.getTaskId() );
        taskComment.setTimestamp( taskCommentDto.getTimestamp() );
        taskComment.setUserId( taskCommentDto.getUserId() );

        return taskComment;
    }

    protected List<TaskComment> taskCommentDtoListToTaskCommentList(List<TaskCommentDto> list) {
        if ( list == null ) {
            return null;
        }

        List<TaskComment> list1 = new ArrayList<TaskComment>( list.size() );
        for ( TaskCommentDto taskCommentDto : list ) {
            list1.add( taskCommentDtoToTaskComment( taskCommentDto ) );
        }

        return list1;
    }
}
