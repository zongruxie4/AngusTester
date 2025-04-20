package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisInfoListRepo;
import cloud.xcan.angus.core.tester.domain.apis.ApisInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ApisInfoSearchRepoMysql extends AbstractSearchRepository<ApisBasicInfo> implements
    ApisInfoSearchRepo {

  @Resource
  private ApisInfoListRepo apisInfoListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ApisBasicInfo> mainClz,
      Object[] params, String... matches) {
    return apisInfoListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria, "apis", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return apisInfoListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
