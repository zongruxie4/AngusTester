package cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.trash.FuncTrashSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.trash.FuncTrashDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class FuncTrashAssembler {

  public static FuncTrashDetailVo toDetailVo(FuncTrash trash) {
    return new FuncTrashDetailVo().setId(trash.getId())
        .setProjectId(trash.getProjectId())
        .setTargetId(trash.getTargetId())
        .setTargetType(trash.getTargetType())
        .setTargetName(trash.getName())
        .setCreatedBy(trash.getCreatedBy())
        .setCreatedByName(trash.getCreatedByName())
        .setCreatedByAvatar(trash.getCreatedByAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static Set<SearchCriteria> getSearchCriteria(FuncTrashSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
  }
}
