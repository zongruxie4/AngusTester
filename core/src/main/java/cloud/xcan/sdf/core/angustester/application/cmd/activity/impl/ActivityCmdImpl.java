package cloud.xcan.sdf.core.angustester.application.cmd.activity.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isUserAction;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
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
  public void batchAdd(Collection<Activity> activities) {
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




