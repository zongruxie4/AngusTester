package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto.ScriptStatisticsDto;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class AnalysisCommonAssembler {

  public static Set<SearchCriteria> getSearchCriteria(ScriptStatisticsDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .orderByFields("id", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate")
        .inAndNotFields("id", "type", "source", "sourceId", "scenarioId", "tag", "createdBy")
        .matchSearchFields("name", "description", "extSearchMerge", "plugin")
        .build();
  }

}
