package cloud.xcan.sdf.core.angustester.domain.scenario;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioListRepo extends CustomBaseRepository<Scenario> {

  StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params);

}
