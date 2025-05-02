package cloud.xcan.angus.core.tester.application.query.activity.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.activity.ActivitySearch;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivitySearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
      protected Page<Activity> process() {
        Page<Activity> page = activitySearchRepo.find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          activityQuery.setProjectName(page);
          userManager.setUserNameAndAvatar(page.getContent(), "userId", "fullName", "avatar");
        }
        return page;
      }
    }.execute();
  }
}
