package cloud.xcan.angus.core.tester.application.query.indicator.impl;

import static cloud.xcan.angus.core.tester.application.converter.IndicatorStabilityConverter.toIndicatorStability;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.core.tester.application.converter.IndicatorStabilityConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStabilityRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorStabilityQueryImpl implements IndicatorStabilityQuery {

  @Resource
  private IndicatorStabilityRepo indicatorStabilityRepo;

  @Resource
  private IndicatorStabilityListRepo indicatorStabilityListRepo;

  @Resource
  private SettingTenantManager settingTenantManager;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public IndicatorStability find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorStability>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IndicatorStability process() {
        IndicatorStability stabilizeDb = indicatorStabilityRepo
            .findByTargetIdAndTargetType(targetId, targetType);
        assertResourceNotFound(nonNull(stabilizeDb), targetId, "IndicatorStability");
        assembleTargetName(stabilizeDb, targetType, targetId);
        return stabilizeDb;
      }
    }.execute();
  }

  @Override
  public IndicatorStability detailAndDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorStability>() {

      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IndicatorStability process() {
        // First query whether there are stability indicators set by the user
        IndicatorStability stabilityDb = indicatorStabilityRepo.findByTargetIdAndTargetType(
            targetId, targetType);
        if (nonNull(stabilityDb)) {
          return stabilityDb;
        }

        // Query the default stability indicators of the platform
        try {
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());
          IndicatorStability stability = toIndicatorStability(setting.getStabilityData(), targetId, targetType);
          assembleTargetName(stability, targetType, targetId);
          return stability;
        } catch (Exception e) {
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  @Override
  public Page<IndicatorStability> list(GenericSpecification<IndicatorStability> spec,
      PageRequest pageable, Class<IndicatorStability> clz) {
    return new BizTemplate<Page<IndicatorStability>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorStability> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());
        return indicatorStabilityListRepo.find(spec.getCriteria(), pageable, clz,
            IndicatorStabilityConverter::objectArrToStability, null);
      }
    }.execute();
  }

  @Override
  public IndicatorStability find0(Long targetId, CombinedTargetType targetType) {
    return indicatorStabilityRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  private void assembleTargetName(IndicatorStability stability,
      CombinedTargetType targetType, Long targetId) {
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    stability.setTargetName(combinedTarget.getTargetName());
  }

}




