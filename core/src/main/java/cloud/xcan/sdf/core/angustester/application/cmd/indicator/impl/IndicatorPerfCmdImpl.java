package cloud.xcan.sdf.core.angustester.application.cmd.indicator.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.INDICATOR_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorType.PERF;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

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
        if (ObjectUtils.isNotEmpty(perfs)) {
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
   * After deletion, the default indicators will be used and do not require review.
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
        indicatorPerfRepo.deleteAllByTargetIdIn(targetIds, targetType.getValue());
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<IndicatorPerf, Long> getRepository() {
    return this.indicatorPerfRepo;
  }
}




