package cloud.xcan.angus.core.tester.domain.script;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.script.count.ScriptCount;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoListRepo extends CustomBaseRepository<ScriptInfo> {

  StringBuilder getSqlTemplate0(SearchMode mode, Class<ScriptInfo> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches);

  ScriptCount count(Set<SearchCriteria> criteria);
}
