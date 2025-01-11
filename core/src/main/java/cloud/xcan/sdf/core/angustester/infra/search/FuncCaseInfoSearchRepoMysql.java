package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoListRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

/**
 * @author xiaolong.liu
 */
@Repository
public class FuncCaseInfoSearchRepoMysql extends AbstractSearchRepository<FuncCaseInfo> implements
    FuncCaseInfoSearchRepo {

  @Resource
  private FuncCaseInfoListRepo funcCaseInfoListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return funcCaseInfoListRepo.getSqlTemplate0(getSearchMode(), step, criterias,
        "func_case", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return funcCaseInfoListRepo.getReturnFieldsCondition(criterias, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
