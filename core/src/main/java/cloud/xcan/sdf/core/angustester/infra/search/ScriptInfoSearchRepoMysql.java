package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfoListRepo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
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
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criteria, Object[] params, String... matches) {
    return scriptInfoListRepo.getSqlTemplate0(getSearchMode(), step, criteria,
        "script", matches);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
