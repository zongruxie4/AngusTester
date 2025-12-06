package cloud.xcan.angus.core.tester.application.cmd.config.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toUpdateGlobalResourceActivity;
import static cloud.xcan.angus.core.tester.application.converter.SettingTenantConverter.initTenantSetting;
import static cloud.xcan.angus.core.tester.infra.util.LinkedHashMapDiffUtil.compareEvaluationMaps;
import static cloud.xcan.angus.core.tester.infra.util.ObjectDiffUtil.compareObjects;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.config.SettingTenantCmd;
import cloud.xcan.angus.core.tester.application.query.config.SettingTenantQuery;
import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.GlobalResourceType;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSetting;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSettingRepo;
import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of tenant setting command operations.
 * </p>
 * <p>
 * Manages tenant-level settings including locale, security, API proxy, and performance indicator
 * configurations.
 * </p>
 * <p>
 * Provides tenant setting initialization, updates, and cache management for real-time configuration
 * updates.
 * </p>
 */
@Biz
@Slf4j
public class SettingTenantCmdImpl extends CommCmd<TenantSetting, Long> implements SettingTenantCmd {

  @Resource
  private TenantSettingRepo settingTenantRepo;
  @Resource
  private SettingTenantQuery settingTenantQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Replaces tenant API proxy settings.
   * </p>
   * <p>
   * Updates server API proxy configuration for the tenant.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void proxyReplace(ServerApiProxy apiProxy) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setServerApiProxyData(apiProxy);
        updateTenantSetting(getOptTenantId(), setting);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Replaces tenant tester event settings.
   * </p>
   * <p>
   * Updates tester event configuration for the tenant.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testerEventReplace(List<TesterEvent> testerEvent) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setTesterEventData(testerEvent);
        updateTenantSetting(getOptTenantId(), setting);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void evaluationReplace(LinkedHashMap<EvaluationPurpose, Integer> evaluation) {
    new BizTemplate<Void>() {

      @Override
      protected void checkParams() {
        assertTrue(evaluation.values().stream().noneMatch(x -> isNull(x) || x < 0 || x > 100),
            "The weight value of the evaluation indicator cannot be empty and must be between 0 and 100.");
        assertTrue(
            evaluation.values().stream().mapToInt(weight -> weight == null ? 0 : weight).sum()
                <= 100,
            "The weight value of the evaluation indicator cannot be empty and must be between 0 and 100.");
      }

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setEvaluationWeightData(evaluation);
        updateTenantSetting(getOptTenantId(), setting);

        activityCmd.add(toUpdateGlobalResourceActivity(CombinedTargetType.INDICATOR,
            GlobalResourceType.EVALUATION, "INDICATOR_UPDATE_TO",
            compareEvaluationMaps(setting.getEvaluationWeightData(), evaluation)));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Replaces tenant function indicator settings.
   * </p>
   * <p>
   * Updates function performance indicator configuration for the tenant.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void funcReplace(FuncData data) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setFuncData(data);
        updateTenantSetting(getOptTenantId(), setting);

        activityCmd.add(toUpdateGlobalResourceActivity(CombinedTargetType.INDICATOR,
            GlobalResourceType.EVALUATION, "INDICATOR_UPDATE_TO",
            compareObjects(setting.getFuncData(), data)));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Replaces tenant performance indicator settings.
   * </p>
   * <p>
   * Updates performance indicator configuration for the tenant.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void perfReplace(PerfData data) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setPerfData(data);
        updateTenantSetting(getOptTenantId(), setting);

        activityCmd.add(toUpdateGlobalResourceActivity(CombinedTargetType.INDICATOR,
            GlobalResourceType.EVALUATION, "INDICATOR_UPDATE_TO",
            compareObjects(setting.getFuncData(), data)));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Replaces tenant data indicator settings.
   * </p>
   * <p>
   * Updates data indicator configuration for the tenant.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void stabilityReplace(StabilityData data) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        TenantSetting setting = settingTenantQuery.findAndInit(getOptTenantId());
        setting.setStabilityData(data);
        updateTenantSetting(getOptTenantId(), setting);

        activityCmd.add(toUpdateGlobalResourceActivity(CombinedTargetType.INDICATOR,
            GlobalResourceType.EVALUATION, "INDICATOR_UPDATE_TO",
            compareObjects(setting.getFuncData(), data)));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Updates tenant setting with cache eviction.
   * </p>
   * <p>
   * Saves tenant setting to database and evicts related cache entries to ensure real-time updates
   * across the system.
   * </p>
   */
  @CacheEvict(key = "'key_' + #tenantId", value = "settingTenant")
  public void updateTenantSetting(Long tenantId, TenantSetting setting) {
    settingTenantRepo.save(setting);
  }

  /**
   * <p>
   * Initializes tenant settings.
   * </p>
   * <p>
   * Creates default tenant settings with timezone configuration when tenant settings don't exist.
   * </p>
   */
  @Override
  public TenantSetting init(Long tenantId) {
    TenantSetting tenantSetting = null;
    if (!settingTenantRepo.existsByTenantId(tenantId)) {
      tenantSetting = initTenantSetting(tenantId);
      settingTenantRepo.save(tenantSetting);
    }
    return tenantSetting;
  }

  @Override
  protected BaseRepository<TenantSetting, Long> getRepository() {
    return this.settingTenantRepo;
  }
}
