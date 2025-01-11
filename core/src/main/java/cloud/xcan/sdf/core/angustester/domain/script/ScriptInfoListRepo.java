package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.script.count.ScriptCount;
import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoListRepo extends CustomBaseRepository<ScriptInfo> {

  StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches);

  ScriptCount count(Set<SearchCriteria> criterias);
}
