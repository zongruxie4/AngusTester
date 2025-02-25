package cloud.xcan.sdf.core.angustester.application.cmd.indicator.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.INDICATOR_ADD;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.INDICATOR_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorType.STABILITY;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilityRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
   * After deletion, the default indicators will be used and do not require review
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        indicatorStabilityRepo.deleteAllByTargetIdIn(targetIds, targetType.getValue());
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<IndicatorStability, Long> getRepository() {
    return this.indicatorStabilityRepo;
  }
}




