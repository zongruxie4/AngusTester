package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityListRepo;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivitySearchRepo;
import cloud.xcan.sdf.core.jpa.repository.AbstractSearchRepository;
import cloud.xcan.sdf.core.jpa.repository.SearchMode;
import java.util.Set;
import javax.annotation.Resource;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.stereotype.Repository;

@Repository
public class ActivitySearchRepoMysql extends AbstractSearchRepository<Activity>
    implements ActivitySearchRepo {

  @Resource
  private ActivityListRepo activityListRepo;

  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] objects, String... matches) {
    return activityListRepo.getSqlTemplate0(getSearchMode(), step, criterias,
        "activity", matches);
  }

  @Override
  public String getReturnFieldsCondition(Set<SearchCriteria> criterias, Object[] params) {
    return activityListRepo.getReturnFieldsCondition(criterias, params);
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
