package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncCaseSearch {

  Page<FuncCaseInfo> search(boolean exportFlag, Set<SearchCriteria> criterias, Pageable pageable,
      Class<FuncCaseInfo> clz, String... matches);

}




