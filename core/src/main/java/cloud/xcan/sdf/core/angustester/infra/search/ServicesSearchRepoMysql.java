package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesListRepo;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
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
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... matches) {
    return servicesListRepo.getSqlTemplate0(getSearchMode(), step, criterias, "services", matches);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }

}
