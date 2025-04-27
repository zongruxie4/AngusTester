package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoListRepo;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ScriptInfoSearchRepoMysql extends SimpleSearchRepository<ScriptInfo>
    implements ScriptInfoSearchRepo {

  @Resource
  private ScriptInfoListRepo scriptInfoListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<ScriptInfo> mainClz,
      Object[] params, String... matches) {
    return scriptInfoListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria,
        "script", matches);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
