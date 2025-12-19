package cloud.xcan.angus.core.tester.application.query.config.impl;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static java.util.Objects.isNull;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.config.SettingTenantCmd;
import cloud.xcan.angus.core.tester.application.query.config.SettingTenantQuery;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSetting;
import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSettingRepo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of tenant setting query operations.
 * </p>
 * <p>
 * Manages tenant setting retrieval, validation, and timezone configuration. Provides comprehensive
 * tenant setting querying with locale support.
 * </p>
 * <p>
 * Supports tenant setting detail retrieval, timezone management, and setting validation for
 * comprehensive tenant configuration administration.
 * </p>
 */
@Service
public class SettingTenantQueryImpl implements SettingTenantQuery {

  @Resource
  private TenantSettingRepo settingTenantRepo;

  @Resource
  private SettingTenantCmd settingTenantCmd;

  /**
   * <p>
   * Retrieves detailed tenant setting information by tenant ID.
   * </p>
   * <p>
   * Fetches complete tenant setting record with timezone configuration. Loads default timezone from
   * application configuration.
   * </p>
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public TenantSetting findAndInit(Long tenantId) {
    return new BizTemplate<TenantSetting>() {

      @Override
      protected TenantSetting process() {
        TenantSetting settingTenant = find0(tenantId);

        if (isNull(settingTenant)) {
          settingTenant = settingTenantCmd.init(getOptTenantId());
        }
        return settingTenant;
      }
    }.execute();
  }

  /**
   * <p>
   * Retrieves tenant setting by tenant ID without validation.
   * </p>
   * <p>
   * Returns tenant setting without existence validation. Returns null if tenant setting does not
   * exist.
   * </p>
   */
  @Override
  public TenantSetting find0(Long tenantId) {
    return settingTenantRepo.findByTenantId(tenantId).orElse(null);
  }
}
