package cloud.xcan.angus.core.tester.application.query.activity.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.tester.application.converter.ActivityConverter;
import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityListRepo;
import cloud.xcan.angus.core.tester.domain.activity.ActivityRepo;
import cloud.xcan.angus.core.tester.domain.activity.summary.ActivitySummary;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.repository.summary.SummaryQueryRegister;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
@SummaryQueryRegister(name = "Activity", table = "activity", groupByColumns = {"opt_date",
    "target_type"})
public class ActivityQueryImpl implements ActivityQuery {

  @Resource
  private ActivityRepo activityRepo;

  @Resource
  private ActivityListRepo activityListRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Activity> find(GenericSpecification<Activity> spec, Pageable pageable) {
    return new BizTemplate<Page<Activity>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Activity> process() {
        Page<Activity> page = activityListRepo.find(spec.getCriteria(), pageable,
            Activity.class, null);
        if (page.hasContent()) {
          setProjectName(page);
          userManager.setUserNameAndAvatar(page.getContent(), "userId", "fullName", "avatar");
        }
        return page;
      }
    }.execute();
  }

  @Override
  public void setProjectName(Page<Activity> page) {
    Map<Long, Project> projectMap = projectQuery.find0ById(page.getContent()
            .stream().map(Activity::getProjectId).collect(Collectors.toSet()))
        .stream().collect(Collectors.toMap(Project::getId, p -> p));
    for (Activity activity : page.getContent()) {
      if (projectMap.containsKey(activity.getProjectId())) {
        activity.setProjectName(projectMap.get(activity.getProjectId()).getName());
      } else {
        activity.setProjectName("--");
      }
    }
  }

  @Override
  public List<ActivitySummary> findSummaryByTarget(CombinedTargetType targetType, Long targetId) {
    List<Activity> activities = activityRepo.findByTargetTypeAndTargetId(targetType, targetId);
    if (isEmpty(activities)) {
      return null;
    }
    userManager.setUserNameAndAvatar(activities, "userId", "fullName", "avatar");
    return activities.stream().map(ActivityConverter::toActivitySummary)
        .collect(Collectors.toList());
  }

  @Override
  public int getActivityNumByMainTarget(Long id) {
    return activityRepo.countAllByMainTargetId(id);
  }

}
