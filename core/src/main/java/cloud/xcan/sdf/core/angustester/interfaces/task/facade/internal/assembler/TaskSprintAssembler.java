package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.TaskSprintUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.TaskSprintDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskSprintAssembler {

  public static TaskSprint addDtoToDomain(TaskSprintAddDto dto) {
    return new TaskSprint()
        .setProjectId(dto.getProjectId())
        .setName(dto.getName())
        .setStatus(TaskSprintStatus.PENDING)
        .setAuthFlag(nullSafe(dto.getAuthFlag(), false))
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setOwnerId(dto.getOwnerId())
        .setTaskPrefix(dto.getTaskPrefix())
        .setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setAttachments(dto.getAttachments())
        .setAcceptanceCriteria(dto.getAcceptanceCriteria())
        .setOtherInformation(dto.getOtherInformation())
        .setDeletedFlag(false);
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
        .setAuthFlag(nonNull(dto.getId()) ? null : nullSafe(dto.getAuthFlag(), false))
        .setStartDate(dto.getStartDate())
        .setDeadlineDate(dto.getDeadlineDate())
        .setOwnerId(dto.getOwnerId())
        // Modifying prefix not allowed
        .setTaskPrefix(nonNull(dto.getId()) ? null : dto.getTaskPrefix())
        .setEvalWorkloadMethod(dto.getEvalWorkloadMethod())
        .setAttachments(dto.getAttachments())
        .setAcceptanceCriteria(dto.getAcceptanceCriteria())
        .setOtherInformation(dto.getOtherInformation())
        .setDeletedFlag(nonNull(dto.getId()) ? null : false);
  }

  public static TaskSprintDetailVo toDetailVo(TaskSprint sprint) {
    return new TaskSprintDetailVo()
        .setId(sprint.getId())
        .setProjectId(sprint.getProjectId())
        .setName(sprint.getName())
        .setStatus(sprint.getStatus())
        .setAuthFlag(sprint.getAuthFlag())
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
        .setLastModifiedBy(sprint.getLastModifiedBy())
        .setLastModifiedDate(sprint.getLastModifiedDate())
        .setTaskNum(sprint.getTaskNum())
        .setValidNum(sprint.getValidTaskNum())
        .setProgress(sprint.getProgress())
        .setMembers(sprint.getMembers());
  }

  public static GenericSpecification<TaskSprint> getSpecification(TaskSprintFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "startDate", "deadlineDate")
        .orderByFields("id", "name", "ownerId", "createdBy", "createdDate", "lastModifiedBy"
            , "startDate", "deadlineDate")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(TaskSprintSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate", "startDate", "deadlineDate")
        .orderByFields("id", "name", "ownerId", "createdBy", "createdDate", "lastModifiedBy"
            , "startDate", "deadlineDate")
        .matchSearchFields("name", "description")
        .build();
  }
}
