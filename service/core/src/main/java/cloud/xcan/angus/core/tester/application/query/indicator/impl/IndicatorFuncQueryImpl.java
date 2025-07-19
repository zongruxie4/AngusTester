package cloud.xcan.angus.core.tester.application.query.indicator.impl;


import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.IndicatorFuncConverter.toIndicatorFunc;
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
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.IndicatorFuncConverter;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncListRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncRepo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFuncSearchRepo;
import cloud.xcan.angus.remote.message.SysException;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorFuncQueryImpl implements IndicatorFuncQuery {

  @Resource
  private IndicatorFuncRepo indicatorFuncRepo;

  @Resource
  private IndicatorFuncListRepo indicatorFuncListRepo;

  @Resource
  private IndicatorFuncSearchRepo indicatorPerfSearchRepo;

  @Resource
  private SettingTenantManager settingTenantManager;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public IndicatorFunc find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorFunc>() {

      @Override
      protected IndicatorFunc process() {
        IndicatorFunc funcDb = indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
        assertResourceNotFound(isNotEmpty(funcDb), targetId, "IndicatorFunc");
        assembleTargetName(funcDb, targetType, targetId);
        return funcDb;
      }
    }.execute();
  }

  @Override
  public IndicatorFunc detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorFunc>() {

      @Override
      protected IndicatorFunc process() {
        // First query whether there are func indicators set by the user
        IndicatorFunc funcDb = indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
        if (nonNull(funcDb)) {
          return funcDb;
        }

        // Query the default func indicators of the platform
        try {
          SettingTenant setting = settingTenantManager.checkAndFindSetting(getOptTenantId());
          IndicatorFunc func = toIndicatorFunc(setting.getFuncData(), targetId, targetType);
          assembleTargetName(func, targetType, targetId);
          return func;
        } catch (Exception e) {
          throw SysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  @Override
  public Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<IndicatorFunc>>() {
      @Override
      protected Page<IndicatorFunc> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriteria());

        return fullTextSearch ? indicatorPerfSearchRepo.find(spec.getCriteria(),
            pageable, IndicatorFunc.class, IndicatorFuncConverter::objectArrToFunc, match)
            : indicatorFuncListRepo.find(spec.getCriteria(),
                pageable, IndicatorFunc.class, IndicatorFuncConverter::objectArrToFunc, null);
      }
    }.execute();
  }

  @Override
  public IndicatorFunc find0(Long targetId, CombinedTargetType targetType) {
    return indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  private void assembleTargetName(IndicatorFunc func, CombinedTargetType targetType,
      Long targetId) {
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    func.setTargetName(combinedTarget.getTargetName());
  }
}
