package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskSprintAssembler {

  public static TaskSprint addDtoToDomain(TaskSprintAddDto dto) {
    return new TaskSprint()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setStatus(TaskSprintStatus.PENDING)
        .setAuth(nullSafe(dto.getAuth(), false))
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setOwnerId(dto.getOwnerId())
        .setTaskPrefix(dto.getTaskPrefix())
        .setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setAttachments(dto.getAttachments())
        .setAcceptanceCriteria(dto.getAcceptanceCriteria())
        .setOtherInformation(dto.getOtherInformation())
        .setDeleted(false);
  }

  public static TaskSprint updateDtoToDomain(TaskSprintUpdateDto dto) {
    return new TaskSprint()
        .setId(dto.getId())
        //.setDirId(dto.getDirId())
        .setName(dto.getName())
        //.setStatus(dto.getStatus())
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setOwnerId(dto.getOwnerId())
        // Not allowed modification
        //.setTaskPrefix(dto.getCasePrefix())
        .setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setAttachments(dto.getAttachments())
        .setAcceptanceCriteria(dto.getAcceptanceCriteria())
        .setOtherInformation(dto.getOtherInformation());
  }

  public static TaskSprint replaceDtoToDomain(TaskSprintReplaceDto dto) {
    return new TaskSprint()
        .setId(dto.getId())
        // Modifying directory not allowed
        .setProjectId(nonNull(dto.getId()) ? null : dto.getProjectId())
        .setName(dto.getName())
        .setStatus(nonNull(dto.getId()) ? null : TaskSprintStatus.PENDING)
        .setAuth(nonNull(dto.getId()) ? null : nullSafe(dto.getAuth(), false))
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setOwnerId(dto.getOwnerId())
        // Modifying prefix not allowed
        .setTaskPrefix(nonNull(dto.getId()) ? null : dto.getTaskPrefix())
        .setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setAttachments(dto.getAttachments())
        .setAcceptanceCriteria(dto.getAcceptanceCriteria())
        .setOtherInformation(dto.getOtherInformation())
        .setDeleted(nonNull(dto.getId()) ? null : false);
  }

  public static TaskSprintDetailVo toDetailVo(TaskSprint sprint) {
    return new TaskSprintDetailVo()
        .setId(sprint.getId())
        .setProjectId(sprint.getProjectId())
        .setName(sprint.getName())
        .setStatus(sprint.getStatus())
        .setAuth(sprint.getAuth())
        .setStartDate(sprint.getStartDate())
        .setDeadlineDate(sprint.getDeadlineDate())
        .setOwnerId(sprint.getOwnerId())
        .setOwnerName(sprint.getOwnerName())
        .setOwnerAvatar(sprint.getOwnerAvatar())
        .setTaskPrefix(sprint.getTaskPrefix())
        .setEvalWorkloadMethod(sprint.getEvalWorkloadMethod())
        .setAttachments(sprint.getAttachments())
        .setAcceptanceCriteria(sprint.getAcceptanceCriteria())
        .setMeetings(isNotEmpty(sprint.getMeetings()) ? sprint.getMeetings().stream()
            .map(TaskMeetingAssembler::toDetailVo).collect(
                Collectors.toList()) : null)
        .setOtherInformation(sprint.getOtherInformation())
        .setTenantId(sprint.getTenantId())
        .setCreatedBy(sprint.getCreatedBy())
        .setCreatedDate(sprint.getCreatedDate())
        .setModifiedBy(sprint.getModifiedBy())
        .setModifiedDate(sprint.getModifiedDate())
        .setTaskNum(sprint.getTaskNum())
        .setValidNum(sprint.getValidTaskNum())
        .setProgress(sprint.getProgress())
        .setMembers(sprint.getMembers());
  }

  public static GenericSpecification<TaskSprint> getSpecification(TaskSprintFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "startDate", "deadlineDate")
        .orderByFields("id", "name", "ownerId", "createdBy", "createdDate", "modifiedBy"
            , "startDate", "deadlineDate")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

}
