package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;


import static cloud.xcan.sdf.core.angustester.application.converter.IndicatorPerfConverter.toIndicatorPerf;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.sdf.api.manager.SettingTenantManager;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorPerfConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.sdf.core.angustester.domain.CombinedTarget;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerfRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorPerfQueryImpl implements IndicatorPerfQuery {

  @Resource
  private IndicatorPerfRepo indicatorPerfRepo;

  @Resource
  private IndicatorPerfListRepo indicatorPerfListRepo;

  @Resource
  private SettingTenantManager settingTenantManager;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public IndicatorPerf find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorPerf>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IndicatorPerf process() {
        IndicatorPerf perfDb = indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
        ProtocolAssert.assertResourceNotFound(isNotEmpty(perfDb), targetId, "IndicatorPerf");
        assembleTargetName(perfDb, targetType, targetId);
        return perfDb;
      }
    }.execute();
  }

  @Override
  public IndicatorPerf detailAndDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorPerf>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IndicatorPerf process() {
        // First query whether there are performance indicators set by the user
        IndicatorPerf perfDb = indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
        if (nonNull(perfDb)) {
          return perfDb;
        }

        // Query the default performance indicators of the platform
        try {
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());
          IndicatorPerf perf = toIndicatorPerf(setting.getPerfData(), targetId, targetType);
          assembleTargetName(perf, targetType, targetId);
          return perf;
        } catch (Exception e) {
          throw CommSysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  @Override
  public Page<IndicatorPerf> list(GenericSpecification<IndicatorPerf> spec,
      PageRequest pageable, Class<IndicatorPerf> clz) {
    return new BizTemplate<Page<IndicatorPerf>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorPerf> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriterias());

        return indicatorPerfListRepo.find(spec.getCriterias(),
            pageable, clz, IndicatorPerfConverter::objectArrToPerf, null);
      }
    }.execute();
  }

  @Override
  public IndicatorPerf find0(Long targetId, CombinedTargetType targetType) {
    return indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  private void assembleTargetName(IndicatorPerf perf, CombinedTargetType targetType,
      Long targetId) {
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    perf.setTargetName(combinedTarget.getTargetName());
  }
}