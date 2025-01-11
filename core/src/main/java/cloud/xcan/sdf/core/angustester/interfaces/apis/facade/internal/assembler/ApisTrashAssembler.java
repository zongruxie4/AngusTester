package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.trash.ApisTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.trash.ApisTrashDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ApisTrashAssembler {

  public static ApisTrashDetailVo toDetailVo(ApisTrash trash) {
    return new ApisTrashDetailVo().setId(trash.getId())
        .setProjectId(trash.getProjectId())
        .setTargetName(trash.getName())
        .setTargetType(trash.getTargetType())
        .setTargetId(trash.getTargetId())
        .setCreatedBy(trash.getCreatedBy())
        .setCreatedByName(trash.getCreatedByName())
        .setCreatedByAvatar(trash.getCreatedByAvatar())
        .setDeletedBy(trash.getDeletedBy())
        .setDeletedByName(trash.getDeletedByName())
        .setDeletedByAvatar(trash.getDeletedByAvatar())
        .setDeletedDate(trash.getDeletedDate());
  }

  public static Set<SearchCriteria> getSearchCriteria(ApisTrashSearchDto dto) {
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
  }
}




