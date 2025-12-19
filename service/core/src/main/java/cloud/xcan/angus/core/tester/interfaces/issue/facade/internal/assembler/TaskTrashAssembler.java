package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrash;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.trash.TaskTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class TaskTrashAssembler {

  public static TaskTrashDetailVo toDetailVo(TaskTrash trash) {
    return new TaskTrashDetailVo().setId(trash.getId())
        .setProjectId(trash.getProjectId())
        .setTargetId(trash.getTargetId())
        .setTargetType(trash.getTargetType())
        .setTargetName(trash.getName())
        .setTaskType(trash.getTaskType())
        .setCreatedBy(trash.getCreatedBy())
        .setCreator(trash.getCreator())
        .setCreatorAvatar(trash.getCreatorAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static GenericSpecification<TaskTrash> getSpecification(TaskTrashListDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
    return new GenericSpecification<>(filters);
  }
}
