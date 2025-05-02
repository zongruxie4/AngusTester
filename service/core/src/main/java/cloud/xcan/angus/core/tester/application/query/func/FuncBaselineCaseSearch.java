package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineCaseSearch {

  Page<FuncBaselineCaseInfo> search(Long baselineId, boolean export,
      Set<SearchCriteria> criteria, PageRequest pageable, Class<FuncBaselineCaseInfo> clz,
      String... matches);

}
