package cloud.xcan.angus.core.tester.interfaces.project.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.project.trash.ProjectTrash;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.trash.ProjectTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.trash.ProjectTrashDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
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
