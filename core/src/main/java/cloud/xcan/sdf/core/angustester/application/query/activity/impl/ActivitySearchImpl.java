package cloud.xcan.sdf.core.angustester.application.query.activity.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.activity.ActivitySearch;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivitySearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ActivitySearchImpl implements ActivitySearch {

  @Resource
  private ActivitySearchRepo activitySearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Activity> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<Activity> clz, String... matches) {
    return new BizTemplate<Page<Activity>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Activity> process() {
        Page<Activity> activityPage = activitySearchRepo.find(criterias, pageable, clz, matches);
        if (activityPage.hasContent()) {
          userManager.setUserNameAndAvatar(activityPage.getContent(),
              "userId", "fullname", "avatar");
        }
        return activityPage;
      }
    }.execute();
  }
}
