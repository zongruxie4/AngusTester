package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseInfoListRepo extends CustomBaseRepository<FuncCaseInfo> {

  StringBuilder getSqlTemplate0(SearchMode mode, Class<FuncCaseInfo> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);

  Page<Long> groups(Set<SearchCriteria> criteria, Pageable pageable, String... matches);

  FuncCaseCount count(Set<SearchCriteria> criteria);

}
