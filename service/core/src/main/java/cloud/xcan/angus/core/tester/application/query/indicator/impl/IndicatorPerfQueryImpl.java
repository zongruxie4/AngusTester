package cloud.xcan.angus.core.tester.application.query.indicator.impl;


import static cloud.xcan.angus.core.tester.application.converter.IndicatorPerfConverter.toIndicatorPerf;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.angus.api.manager.SettingTenantManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.IndicatorPerfConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfRepo;
import cloud.xcan.angus.remote.message.SysException;
import jakarta.annotation.Resource;
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
      protected IndicatorPerf process() {
        IndicatorPerf perfDb = indicatorPerfRepo.findByTargetIdAndTargetType(targetId, targetType);
        ProtocolAssert.assertResourceNotFound(isNotEmpty(perfDb), targetId, "IndicatorPerf");
        assembleTargetName(perfDb, targetType, targetId);
        return perfDb;
      }
    }.execute();
  }

  @Override
  public IndicatorPerf detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorPerf>() {

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
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  @Override
  public Page<IndicatorPerf> list(GenericSpecification<IndicatorPerf> spec,
      PageRequest pageable, Class<IndicatorPerf> clz) {
    return new BizTemplate<Page<IndicatorPerf>>() {

      @Override
      protected Page<IndicatorPerf> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

        return indicatorPerfListRepo.find(spec.getCriteria(),
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
