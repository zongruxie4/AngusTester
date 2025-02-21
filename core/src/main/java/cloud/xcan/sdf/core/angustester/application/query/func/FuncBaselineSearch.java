package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineSearch {

  Page<FuncBaselineInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<FuncBaselineInfo> clz, String... matches);
}
