package cloud.xcan.sdf.core.angustester.application.query.indicator.impl;


import static cloud.xcan.sdf.core.angustester.application.converter.IndicatorFuncConverter.toIndicatorFunc;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.INDICATOR_GET_PLATFORM_FAIL_CODE;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getOptTenantId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.tenant.SettingTenant;
import cloud.xcan.sdf.api.manager.SettingTenantManager;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.core.angustester.application.converter.IndicatorFuncConverter;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.sdf.core.angustester.domain.CombinedTarget;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFuncListRepo;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFuncRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class IndicatorFuncQueryImpl implements IndicatorFuncQuery {

  @Resource
  private IndicatorFuncRepo indicatorFuncRepo;

  @Resource
  private IndicatorFuncListRepo indicatorFuncListRepo;

  @Resource
  private SettingTenantManager settingTenantManager;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public IndicatorFunc find(Long targetId, CombinedTargetType targetType) {
    return new BizTemplate<IndicatorFunc>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected IndicatorFunc process() {
        IndicatorFunc funcDb = indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
        ProtocolAssert.assertResourceNotFound(isNotEmpty(funcDb), targetId, "IndicatorFunc");
        assembleTargetName(funcDb, targetType, targetId);
        return funcDb;
      }
    }.execute();
  }

  @Override
  public IndicatorFunc detailAndDefault(CombinedTargetType targetType, Long targetId) {
    return new BizTemplate<IndicatorFunc>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

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
          throw CommSysException.of(INDICATOR_GET_PLATFORM_FAIL_CODE, INDICATOR_GET_PLATFORM_FAIL);
        }
      }
    }.execute();
  }

  @Override
  public Page<IndicatorFunc> list(GenericSpecification<IndicatorFunc> spec,
      PageRequest pageable, Class<IndicatorFunc> clz) {
    return new BizTemplate<Page<IndicatorFunc>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<IndicatorFunc> process() {
        // Set authorization conditions when you are not an administrator or only query yourself
        commonQuery.checkAndSetAuthObjectIdCriteria(spec.getCriterias());

        return indicatorFuncListRepo.find(spec.getCriterias(),
            pageable, clz, IndicatorFuncConverter::objectArrToFunc, null);
      }
    }.execute();
  }

  @Override
  public IndicatorFunc find0(Long targetId, CombinedTargetType targetType) {
    return indicatorFuncRepo.findByTargetIdAndTargetType(targetId, targetType);
  }

  private void assembleTargetName(IndicatorFunc func, CombinedTargetType targetType, Long targetId) {
    CombinedTarget combinedTarget = commonQuery.getCombinedTarget(targetType, targetId, true);
    func.setTargetName(combinedTarget.getTargetName());
  }
}