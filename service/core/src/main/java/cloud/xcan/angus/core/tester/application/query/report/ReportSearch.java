package cloud.xcan.angus.core.tester.application.query.report;

import cloud.xcan.angus.core.tester.domain.report.ReportInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ReportSearch {

  Page<ReportInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ReportInfo> clz, String... matches);
}
