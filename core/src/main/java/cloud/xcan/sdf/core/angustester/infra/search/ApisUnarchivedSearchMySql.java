package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ApisUnarchivedSearchMySql extends AbstractSearchRepository<ApisUnarchived>
    implements ApisUnarchivedSearchRepo {

  @Resource
  private ApisUnarchivedListRepo apisUnarchivedListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    return apisUnarchivedListRepo.getSqlTemplate0(getSearchMode(), step, criteria,
        "apis_unarchived", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return apisUnarchivedListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
