package cloud.xcan.angus.core.tester.interfaces.analysis.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto.ScriptStatisticsDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
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
