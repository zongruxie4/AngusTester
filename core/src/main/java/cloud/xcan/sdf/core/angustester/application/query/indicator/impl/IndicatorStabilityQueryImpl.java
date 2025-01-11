package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;

import static cloud.xcan.sdf.core.angustester.application.converter.IndicatorStabilityConverter.toIndicatorStability;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.sdf.api.manager.SettingTenantManager;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorStabilityConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.sdf.core.angustester.domain.CombinedTarget;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilityListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStabilityRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import javax.annotation.Resource;
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
          throw CommSysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
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
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriterias());
        return indicatorStabilityListRepo.find(spec.getCriterias(), pageable, clz,
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




