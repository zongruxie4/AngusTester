package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineCaseSearch {

  Page<FuncBaselineCaseInfo> search(Long baselineId, boolean exportFlag,
      Set<SearchCriteria> criterias, PageRequest pageable, Class<FuncBaselineCaseInfo> clz,
      String... matches);

}
