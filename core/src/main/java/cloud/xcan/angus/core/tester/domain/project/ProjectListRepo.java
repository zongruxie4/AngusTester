package cloud.xcan.angus.core.tester.domain.project;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProjectListRepo extends CustomBaseRepository<Project> {

  StringBuilder getSqlTemplate0(SearchMode mode, Class<Project> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);

}
