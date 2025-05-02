package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.in;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.tester.domain.analysis.AnalysisResource;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.annotations.NonNullable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public class AnalysisConverter {

  public static Set<SearchCriteria> getAnalysisCreatorResourcesFilter(
      @NonNullable AnalysisResource resource, Long projectId, Long sprintId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdIds) {
    Set<SearchCriteria> filters = getAnalysisResourcesFilter(resource, projectId, sprintId,
        startDate, endDate);
    if (isNotEmpty(createdIds)) {
      filters.add(in("createdBy", createdIds));
    }
    return filters;
  }

  public static @NotNull Set<SearchCriteria> getAnalysisResourcesFilter(
      @NonNullable AnalysisResource resource, Long projectId, Long sprintId,
      LocalDateTime startDate, LocalDateTime endDate) {
    Set<SearchCriteria> filters = new HashSet<>();
    filters.add(equal("resource", resource.name()));
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(sprintId)) {
      filters.add(equal("planId", sprintId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    return filters;
  }
}
