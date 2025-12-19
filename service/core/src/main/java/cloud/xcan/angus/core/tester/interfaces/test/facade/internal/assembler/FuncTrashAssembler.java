package cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrash;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.trash.FuncTrashFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.trash.FuncTrashDetailVo;
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
        .setCreator(trash.getCreator())
        .setCreatorAvatar(trash.getCreatorAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static GenericSpecification<FuncTrash> getSpecification(FuncTrashFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
    return new GenericSpecification<>(filters);
  }
}
