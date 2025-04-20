package cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

public class TaskMeetingAssembler {

  public static TaskMeeting addToDomain(TaskMeetingAddDto dto) {
    return new TaskMeeting().setProjectId(dto.getProjectId())
        .setSprintId(dto.getSprintId())
        .setSubject(dto.getSubject())
        .setType(dto.getType())
        .setDate(dto.getDate()).setTime(dto.getTime())
        .setLocation(dto.getLocation()).setModerator(dto.getModerator())
        .setModeratorId(dto.getModerator().getId())
        .setParticipants(dto.getParticipants())
        .setContent(dto.getContent());
  }

  public static TaskMeeting updateToDomain(TaskMeetingUpdateDto dto) {
    return new TaskMeeting().setId(dto.getId())
        .setSprintId(dto.getSprintId())
        .setSubject(dto.getSubject())
        .setType(dto.getType())
        .setDate(dto.getDate()).setTime(dto.getTime())
        .setLocation(dto.getLocation()).setModerator(dto.getModerator())
        .setModeratorId(dto.getModerator().getId())
        .setParticipants(dto.getParticipants())
        .setContent(dto.getContent());
  }

  public static TaskMeeting replaceToDomain(TaskMeetingReplaceDto dto) {
    return new TaskMeeting().setId(dto.getId())
        .setProjectId(dto.getProjectId())
        .setSprintId(dto.getSprintId())
        .setSubject(dto.getSubject())
        .setType(dto.getType())
        .setDate(dto.getDate()).setTime(dto.getTime())
        .setLocation(dto.getLocation()).setModerator(dto.getModerator())
        .setModeratorId(dto.getModerator().getId())
        .setParticipants(dto.getParticipants())
        .setContent(dto.getContent());
  }

  public static TaskMeetingDetailVo toDetailVo(TaskMeeting meeting) {
    return new TaskMeetingDetailVo().setId(meeting.getId())
        .setProjectId(meeting.getProjectId())
        .setSprintId(meeting.getSprintId())
        .setSubject(meeting.getSubject())
        .setType(meeting.getType())
        .setDate(meeting.getDate()).setTime(meeting.getTime())
        .setLocation(meeting.getLocation())
        .setModerator(meeting.getModerator())
        .setParticipants(meeting.getParticipants())
        .setContent(meeting.getContent())
        .setCreatedBy(meeting.getCreatedBy())
        .setCreatedDate(meeting.getCreatedDate())
        .setLastModifiedBy(meeting.getLastModifiedBy())
        .setLastModifiedDate(meeting.getLastModifiedDate());
  }

  public static TaskMeetingVo toVo(TaskMeeting meeting) {
    return new TaskMeetingVo().setId(meeting.getId())
        .setProjectId(meeting.getProjectId())
        .setSprintId(meeting.getSprintId())
        .setSubject(meeting.getSubject())
        .setType(meeting.getType())
        .setDate(meeting.getDate()).setTime(meeting.getTime())
        .setLocation(meeting.getLocation())
        .setModerator(meeting.getModerator())
        .setParticipants(meeting.getParticipants())
        .setCreatedBy(meeting.getCreatedBy())
        .setCreatedDate(meeting.getCreatedDate())
        .setLastModifiedBy(meeting.getLastModifiedBy())
        .setLastModifiedDate(meeting.getLastModifiedDate());
  }

  public static Specification<TaskMeeting> getSpecification(TaskMeetingFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "subject", "createdBy", "createdDate", "lastModifiedBy")
        .matchSearchFields("subject")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(TaskMeetingSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "subject", "createdBy", "createdDate", "lastModifiedBy")
        .matchSearchFields("subject")
        .build();
  }

}
