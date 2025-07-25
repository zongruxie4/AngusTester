package cloud.xcan.angus.core.tester.application.cmd.indicator.impl;

import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.INDICATOR_ADD;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.INDICATOR_UPDATE;
import static cloud.xcan.angus.core.tester.domain.indicator.IndicatorType.STABILITY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for stability indicator management.
 * <p>
 * Provides methods for adding, replacing, and deleting stability indicators.
 * <p>
 * Ensures permission checks, activity logging, and batch operations with transaction management.
 */
@Biz
public class IndicatorStabilityCmdImpl extends CommCmd<IndicatorStability, Long> implements
    IndicatorStabilityCmd {

  @Resource
  private IndicatorStabilityRepo indicatorStabilityRepo;

  @Resource
  private IndicatorStabilityQuery indicatorStabilityQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new stability indicator.
   * <p>
   * Checks target existence and ensures only one indicator per target.
   * <p>
   * Logs indicator add activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(IndicatorStability stability) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ActivityResource target;

      @Override
      protected void checkParams() {
        target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(
            stability.getTargetType(), stability.getTargetId());

        // TODO 验证最大并发数是否满足授权限制(云服务版本查询购买商品并发配额，私有化读取许可文件)
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Update when it exists to ensure that a target corresponds to only one indicator
        IndicatorStability stabilityDb = indicatorStabilityQuery
            .find0(stability.getTargetId(), stability.getTargetType());
        if (nonNull(stabilityDb)) {
          stability.setId(stabilityDb.getId());
        }

        batchReplaceOrInsert(singletonList(stability));

        activityCmd.add(toActivity(stability.getTargetType(), target, INDICATOR_ADD, STABILITY));
        return new IdKey<Long, Object>().setId(stability.getId());
      }
    }.execute();
  }

  /**
   * Replace (add or update) a stability indicator.
   * <p>
   * Checks existence and updates or adds the indicator as needed.
   * <p>
   * Logs indicator update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(IndicatorStability stability) {
    new BizTemplate<Void>() {
      IndicatorStability stabilityDb;
      ActivityResource target;

      @Override
      protected void checkParams() {
        // Check the indicator stability exists
        stabilityDb = indicatorStabilityQuery.find0(stability.getTargetId(),
            stability.getTargetType());

        if (nonNull(stabilityDb)){
          target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(
              stabilityDb.getTargetType(), stabilityDb.getTargetId());
        }
      }

      @Override
      protected Void process() {
        if (isNull(stabilityDb)) {
          add(stability);
          return null;
        }

        stability.setId(stabilityDb.getId());
        indicatorStabilityRepo.save(stability);

        activityCmd.add(toActivity(stabilityDb.getTargetType(), target, INDICATOR_UPDATE,
            STABILITY));
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of stability indicators by IDs.
   * <p>
   * Deletes indicators and logs activity for each deleted indicator.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<IndicatorStability> stabilities = indicatorStabilityRepo.findAllById(ids);
        if (isNotEmpty(stabilities)) {
          indicatorStabilityRepo.deleteAll(stabilities);

          saveActivity(stabilities);
        }
        return null;
      }

      private void saveActivity(List<IndicatorStability> stabilities) {
        List<Activity> activities = new ArrayList<>(ids.size());
        for (IndicatorStability stability : stabilities) {
          Object target = commonQuery.checkAndGetIndicatorTarget(stability.getTargetType(),
              stability.getTargetId());
          activities.add(toActivity(stability.getTargetType(), (ActivityResource) target,
              INDICATOR_UPDATE, STABILITY));
        }
        activityCmd.addAll(activities);
      }
    }.execute();
  }

  /**
   * Delete all stability indicators by target IDs and type.
   * <p>
   * After deletion, default indicators will be used and do not require review.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        indicatorStabilityRepo.deleteAllByTargetIdIn(targetIds, targetType.getValue());
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for stability indicators.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<IndicatorStability, Long> getRepository() {
    return this.indicatorStabilityRepo;
  }
}




