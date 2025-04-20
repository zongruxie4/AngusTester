package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.in;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.remote.search.SearchCriteria;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class TaskMeetingConverter {

  public static Set<SearchCriteria> getMeetingCreatorResourcesFilter(Long projectId,
      Long sprintId, LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdIds) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(sprintId)) {
      filters.add(equal("sprintId", sprintId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    if (isNotEmpty(createdIds)) {
      filters.add(in("createdBy", createdIds));
    }
    return filters;
  }

}
