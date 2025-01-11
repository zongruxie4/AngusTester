package cloud.xcan.sdf.core.angustester.domain.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author xiaolong.liu
 */
@NoRepositoryBean
public interface ApisInfoListRepo extends CustomBaseRepository<ApisBasicInfo> {

  StringBuilder getSqlTemplate0(SearchMode mode, SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String tableName, String... matches);

  String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params);

}
