package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoListRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

/**
 * @author XiaoLong Liu
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
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<FuncCaseInfo> mainClz,
      Object[] params, String... matches) {
    return funcCaseInfoListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria,
        "func_case", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return funcCaseInfoListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
