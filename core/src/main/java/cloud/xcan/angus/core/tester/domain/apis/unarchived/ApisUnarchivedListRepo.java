package cloud.xcan.angus.core.tester.domain.apis.unarchived;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author XiaoLong Liu
 */
@NoRepositoryBean
public interface ApisUnarchivedListRepo extends CustomBaseRepository<ApisUnarchived> {

  StringBuilder getSqlTemplate0(SearchMode mode, Class<ApisUnarchived> mainClz,
      Set<SearchCriteria> criteria, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);

}
