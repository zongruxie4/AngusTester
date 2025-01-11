package cloud.xcan.sdf.core.angustester.interfaces.project.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.project.trash.ProjectTrash;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ProjectTrashAssembler {

  public static ProjectTrashDetailVo toDetailVo(ProjectTrash trash) {
    return new ProjectTrashDetailVo().setId(trash.getId())
        .setTargetName(trash.getName())
        .setTargetId(trash.getTargetId())
        .setCreatedBy(trash.getCreatedBy())
        .setCreatedByName(trash.getCreatedByName())
        .setCreatedByAvatar(trash.getCreatedByAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static Set<SearchCriteria> getSearchCriteria(ProjectTrashSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
  }

}