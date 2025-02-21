package cloud.xcan.sdf.core.angustester.application.query.report;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.report.ReportInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ReportSearch {

  Page<ReportInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<ReportInfo> clz, String... matches);
}
