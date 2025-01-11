package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.trash.ScenarioTrashSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ScenarioTrashAssembler {

  public static ScenarioTrashDetailVo toDetailVo(ScenarioTrash trash) {
    return new ScenarioTrashDetailVo().setId(trash.getId())
        .setProjectId(trash.getProjectId())
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

  public static Set<SearchCriteria> getSearchCriteria(ScenarioTrashSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "deletedDate")
        .orderByFields("id", "deletedDate")
        .matchSearchFields("targetName")
        .build();
  }

}