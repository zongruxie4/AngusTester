package cloud.xcan.angus.core.tester.application.cmd.activity.impl;


import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityRepo;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reuse for functional testing plugin.
 */
@Biz
public class ActivityCmdImpl extends CommCmd<Activity, Long> implements ActivityCmd {

  @Autowired(required = false)  // Prevent injection failure after other service dependencies.
  private ActivityRepo activityRepo;

  /**
   * Complete principal and insert save
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void add(Activity activity) {
    if (!isUserAction() || Objects.isNull(activity)) {
      return;
    }
    insert0(activity);
  }

  /**
   * Complete principal and insert save
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void addAll(Collection<Activity> activities) {
    if (!isUserAction() || ObjectUtils.isEmpty(activities)) {
      return;
    }
    batchInsert0(activities);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByTarget(CombinedTargetType targetType, List<Long> targetIds) {
    activityRepo.deleteByTargetIdAndTargetType(targetIds, targetType.getValue());
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteByTaskIds(List<Long> taskIds) {
    activityRepo.deleteByTaskId(taskIds);
  }

  @Override
  protected BaseRepository<Activity, Long> getRepository() {
    return this.activityRepo;
  }
}




