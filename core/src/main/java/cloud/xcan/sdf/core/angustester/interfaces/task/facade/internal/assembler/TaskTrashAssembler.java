package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.trash.TaskTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.trash.TaskTrashDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
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
        .setCreatedByName(trash.getCreatedByName())
        .setCreatedByAvatar(trash.getCreatedByAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static Set<SearchCriteria> getSearchCriteria(TaskTrashSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
  }
}
