package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncCaseSearch {

  Page<FuncCaseInfo> search(boolean export, Set<SearchCriteria> criteria, Pageable pageable,
      Class<FuncCaseInfo> clz, String... matches);

}




