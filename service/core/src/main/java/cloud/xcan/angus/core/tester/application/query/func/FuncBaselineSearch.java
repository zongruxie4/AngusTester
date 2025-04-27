package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineSearch {

  Page<FuncBaselineInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<FuncBaselineInfo> clz, String... matches);
}
