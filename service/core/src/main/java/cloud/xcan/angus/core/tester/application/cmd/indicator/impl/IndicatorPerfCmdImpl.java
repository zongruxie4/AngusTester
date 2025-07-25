package cloud.xcan.angus.core.tester.application.cmd.indicator.impl;

import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.INDICATOR_UPDATE;
import static cloud.xcan.angus.core.tester.domain.indicator.IndicatorType.PERF;
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
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for performance indicator management.
 * <p>
 * Provides methods for adding, replacing, and deleting performance indicators.
 * <p>
 * Ensures permission checks, activity logging, and batch operations with transaction management.
 */
@Biz
public class IndicatorPerfCmdImpl extends CommCmd<IndicatorPerf, Long> implements IndicatorPerfCmd {

  @Resource
  private IndicatorPerfRepo indicatorPerfRepo;
  @Resource
  private IndicatorPerfQuery indicatorPerfQuery;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new performance indicator.
   * <p>
   * Checks target existence and ensures only one indicator per target.
   * <p>
   * Logs indicator update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(IndicatorPerf perf) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ActivityResource target;

      @Override
      protected void checkParams() {
        target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(perf.getTargetType(),
            perf.getTargetId());

        // TODO 验证最大并发数是否满足授权限制(云服务版本查询购买商品并发配额，私有化读取许可文件)
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Update when it exists to ensure that a target corresponds to only one indicator
        IndicatorPerf perfDb = indicatorPerfQuery.find0(perf.getTargetId(), perf.getTargetType());
        if (nonNull(perfDb)) {
          perf.setId(perfDb.getId());
        }

        batchReplaceOrInsert(singletonList(perf));

        activityCmd.add(toActivity(perf.getTargetType(), target, INDICATOR_UPDATE, PERF));
        return new IdKey<Long, Object>().setId(perf.getId());
      }
    }.execute();
  }

  /**
   * Replace (add or update) a performance indicator.
   * <p>
   * Checks existence and updates or adds the indicator as needed.
   * <p>
   * Logs indicator update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(IndicatorPerf perf) {
    new BizTemplate<Void>() {
      IndicatorPerf perfDb;
      ActivityResource target;

      @Override
      protected void checkParams() {
        // Check the indicator perf exists
        perfDb = indicatorPerfQuery.find0(perf.getTargetId(), perf.getTargetType());
        // TODO 验证最大并发数是否满足授权限制(云服务版本查询购买商品并发配额，私有化读取许可文件)

        if (nonNull(perfDb)){
          target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(
              perfDb.getTargetType(), perfDb.getTargetId());
        }
      }

      @Override
      protected Void process() {
        if (isNull(perfDb)) {
          add(perf);
          return null;
        }

        perf.setId(perfDb.getId());
        indicatorPerfRepo.save(perf);

        activityCmd.add(toActivity(perfDb.getTargetType(), target, INDICATOR_UPDATE, PERF));
        return null;
      }
    }.execute();
  }

  /**
   * Delete a batch of performance indicators by IDs.
   * <p>
   * Deletes indicators and logs activity for each deleted indicator.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOP
      }

      @Override
      protected Void process() {
        List<IndicatorPerf> perfs = indicatorPerfRepo.findAllById(ids);
        if (isNotEmpty(perfs)) {
          indicatorPerfRepo.deleteAll(perfs);

          saveActivity(perfs);
        }
        return null;
      }

      private void saveActivity(List<IndicatorPerf> perfs) {
        List<Activity> activities = new ArrayList<>(ids.size());
        for (IndicatorPerf perf : perfs) {
          Object target = commonQuery.checkAndGetIndicatorTarget(perf.getTargetType(),
              perf.getTargetId());
          activities.add(toActivity(perf.getTargetType(),
              (ActivityResource) target, INDICATOR_UPDATE, PERF));
        }
        activityCmd.addAll(activities);
      }
    }.execute();
  }

  /**
   * Delete all performance indicators by target IDs and type.
   * <p>
   * After deletion, default indicators will be used and do not require review.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void deleteAllByTarget(List<Long> targetIds, CombinedTargetType targetType) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        indicatorPerfRepo.deleteAllByTargetIdIn(targetIds, targetType.getValue());
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for performance indicators.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<IndicatorPerf, Long> getRepository() {
    return this.indicatorPerfRepo;
  }
}




