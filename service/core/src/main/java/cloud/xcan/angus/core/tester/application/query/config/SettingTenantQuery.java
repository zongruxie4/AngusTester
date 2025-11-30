package cloud.xcan.angus.core.tester.application.query.config;


import cloud.xcan.angus.core.tester.domain.config.tenant.TenantSetting;

public interface SettingTenantQuery {

  TenantSetting findAndInit(Long tenantId);

  TenantSetting find0(Long tenantId);

}
