package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedListRepo;
import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchivedSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ApisUnarchived> mainClz,
      Object[] params, String... matches) {
    return apisUnarchivedListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria,
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
