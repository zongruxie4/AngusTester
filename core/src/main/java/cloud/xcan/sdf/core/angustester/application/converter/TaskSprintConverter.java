package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.api.search.SearchCriteria.equal;
import static cloud.xcan.sdf.api.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.sdf.api.search.SearchCriteria.in;
import static cloud.xcan.sdf.api.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.commonlink.TaskTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSprintSummary;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class TaskSprintConverter {

  public static TaskTrash toTrashTask(TaskSprint sprintDb) {
    return new TaskTrash()
        .setId(getCachedUidGenerator().getUID())
        .setProjectId(sprintDb.getProjectId())
        .setSprintId(sprintDb.getId())
        .setTargetType(TaskTargetType.TASK_SPRINT)
        .setTargetId(sprintDb.getId())
        .setTargetName(sprintDb.getName())
        .setCreatedBy(sprintDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static void setReplaceInfo(TaskSprint sprint, TaskSprint sprintDb) {
    sprint.setStatus(sprintDb.getStatus())
        .setProjectId(sprintDb.getProjectId())
        .setAuthFlag(nullSafe(sprintDb.getAuthFlag(), false))
        .setDeletedFlag(sprintDb.getDeletedFlag())
        .setDeletedBy(sprintDb.getDeletedBy())
        .setDeletedDate(sprintDb.getDeletedDate())
        .setTenantId(sprintDb.getTenantId())
        .setCreatedBy(sprintDb.getCreatedBy())
        .setCreatedDate(sprintDb.getCreatedDate());
  }

  public static TaskSprint clone(TaskSprint sprintDb) {
    return new TaskSprint()
        .setProjectId(sprintDb.getProjectId())
        .setAuthFlag(sprintDb.getAuthFlag())
        .setName(sprintDb.getName())
        .setStatus(TaskSprintStatus.PENDING)
        .setStartDate(sprintDb.getStartDate())
        .setDeadlineDate(sprintDb.getDeadlineDate())
        .setOwnerId(sprintDb.getOwnerId())
        .setTaskPrefix(sprintDb.getTaskPrefix())
        .setEvalWorkloadMethod(sprintDb.getEvalWorkloadMethod())
        .setAcceptanceCriteria(sprintDb.getAcceptanceCriteria())
        //.setMeetings(sprintDb.getMeetings())
        .setOtherInformation(sprintDb.getOtherInformation())
        .setDeletedFlag(false);
  }

  public static TaskSprintSummary toSprintSummary(TaskSprint sprintDb) {
    return new TaskSprintSummary().setId(sprintDb.getId())
        .setProjectId(sprintDb.getProjectId())
        .setAuthFlag(sprintDb.getAuthFlag())
        .setName(sprintDb.getName())
        .setStatus(sprintDb.getStatus())
        .setStartDate(sprintDb.getStartDate())
        .setDeadlineDate(sprintDb.getDeadlineDate())
        .setOwnerId(sprintDb.getOwnerId())
        .setOwnerName(sprintDb.getOwnerName())
        .setOwnerAvatar(sprintDb.getOwnerAvatar())
        .setTaskPrefix(sprintDb.getTaskPrefix())
        .setEvalWorkloadMethod(sprintDb.getEvalWorkloadMethod())
        .setAcceptanceCriteria(sprintDb.getAcceptanceCriteria())
        .setMeetings(sprintDb.getMeetings())
        .setOtherInformation(sprintDb.getOtherInformation())
        .setCreatedBy(sprintDb.getCreatedBy())
        .setCreatedDate(sprintDb.getCreatedDate())
        .setLastModifiedBy(sprintDb.getLastModifiedBy())
        .setLastModifiedDate(sprintDb.getLastModifiedDate());
  }

  public static Set<SearchCriteria> getSprintCreatorResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdIds) {
    Set<SearchCriteria> filters = getSprintResourcesFilter(projectId,
        sprintId, startDate, endDate);
    if(isNotEmpty(createdIds)){
      filters.add(in("createdBy", createdIds));
    }
    return filters;
  }

  public static @NotNull Set<SearchCriteria> getSprintResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(sprintId)) {
      filters.add(equal("id", sprintId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    filters.add(equal("deletedFlag", false));
    return filters;
  }
}
