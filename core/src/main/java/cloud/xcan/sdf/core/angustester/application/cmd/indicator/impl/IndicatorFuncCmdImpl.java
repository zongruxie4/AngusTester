package cloud.xcan.sdf.core.angustester.application.cmd.indicator.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.INDICATOR_UPDATE;
import static cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorType.FUNC;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorFuncCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFuncRepo;
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
public class IndicatorFuncCmdImpl extends CommCmd<IndicatorFunc, Long> implements IndicatorFuncCmd {

  @Resource
  private IndicatorFuncRepo indicatorFuncRepo;

  @Resource
  private IndicatorFuncQuery indicatorFuncQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(IndicatorFunc func) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ActivityResource target;

      @Override
      protected void checkParams() {
        target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(func.getTargetType(),
            func.getTargetId());

        // TODO 验证最大并发数是否满足授权限制(云服务版本查询购买商品并发配额，私有化读取许可文件)
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Update when it exists to ensure that a target corresponds to only one indicator
        IndicatorFunc funcDb = indicatorFuncQuery.find0(func.getTargetId(), func.getTargetType());
        if (nonNull(funcDb)) {
          func.setId(funcDb.getId());
        }

        batchReplaceOrInsert(singletonList(func));

        activityCmd.add(toActivity(func.getTargetType(), target, INDICATOR_UPDATE, FUNC));
        return new IdKey<Long, Object>().setId(func.getId());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(IndicatorFunc func) {
    new BizTemplate<Void>() {
      IndicatorFunc funcDb;
      ActivityResource target;

      @Override
      protected void checkParams() {
        // Check the indicator func exists
        funcDb = indicatorFuncQuery.find0(func.getTargetId(), func.getTargetType());
        // TODO 验证最大并发数是否满足授权限制(云服务版本查询购买商品并发配额，私有化读取许可文件)

        if (nonNull(funcDb)){
          target = (ActivityResource) commonQuery.checkAndGetIndicatorTarget(
              funcDb.getTargetType(), funcDb.getTargetId());
        }
      }

      @Override
      protected Void process() {
        if (isNull(funcDb)) {
          add(func);
          return null;
        }

        func.setId(funcDb.getId());
        indicatorFuncRepo.save(func);

        activityCmd.add(toActivity(funcDb.getTargetType(), target, INDICATOR_UPDATE, FUNC));
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
        List<IndicatorFunc> funcs = indicatorFuncRepo.findAllById(ids);
        if (isNotEmpty(funcs)) {
          indicatorFuncRepo.deleteAll(funcs);

          saveActivity(funcs);
        }
        return null;
      }

      private void saveActivity(List<IndicatorFunc> funcs) {
        List<Activity> activities = new ArrayList<>(ids.size());
        for (IndicatorFunc func : funcs) {
          Object target = commonQuery.checkAndGetIndicatorTarget(func.getTargetType(),
              func.getTargetId());
          activities.add(toActivity(func.getTargetType(), (ActivityResource) target,
              INDICATOR_UPDATE, FUNC));
        }
        activityCmd.batchAdd(activities);
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
        indicatorFuncRepo.deleteAllByTargetIdIn(targetIds, targetType.getValue());
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<IndicatorFunc, Long> getRepository() {
    return this.indicatorFuncRepo;
  }
}




