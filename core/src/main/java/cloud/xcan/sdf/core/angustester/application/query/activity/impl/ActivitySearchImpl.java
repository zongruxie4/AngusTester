package cloud.xcan.sdf.core.angustester.application.query.activity.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.activity.ActivityQuery;
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
  private ActivityQuery activityQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Activity> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<Activity> clz, String... matches) {
    return new BizTemplate<Page<Activity>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Activity> process() {
        Page<Activity> page = activitySearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          activityQuery.setProjectName(page);
          userManager.setUserNameAndAvatar(page.getContent(), "userId", "fullname", "avatar");
        }
        return page;
      }
    }.execute();
  }
}
