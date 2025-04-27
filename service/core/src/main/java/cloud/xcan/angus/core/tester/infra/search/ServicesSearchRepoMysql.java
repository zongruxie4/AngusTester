package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.angus.core.jpa.repository.SearchMode;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesListRepo;
import cloud.xcan.angus.core.tester.domain.services.ServicesSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class ServicesSearchRepoMysql extends AbstractSearchRepository<Services> implements
    ServicesSearchRepo {

  @Resource
  private ServicesListRepo servicesListRepo;

  /**
   * Non-main mainClass conditions and joins need to be assembled by themselves
   */
  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<Services> mainClz,
      Object[] params, String... matches) {
    return servicesListRepo.getSqlTemplate0(getSearchMode(), mainClz, criteria, "services",
        matches);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
