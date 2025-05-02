package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoListRepo;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ExecInfoSearchRepoMysql extends SimpleSearchRepository<ExecInfo>
    implements ExecInfoSearchRepo {

  @Resource
  private ExecInfoListRepo scriptInfoListRepo;

  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ExecInfo> mainClz,
      Object[] params, String... matches) {
    return scriptInfoListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria,
        "exec", matches);  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params) {
    return scriptInfoListRepo.getReturnFieldsCondition(criteria, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return super.getSearchMode();
  }

}
