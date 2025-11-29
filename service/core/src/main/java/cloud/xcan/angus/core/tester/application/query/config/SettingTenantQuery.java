package cloud.xcan.angus.core.tester.application.query.config;


import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;

public interface SettingTenantQuery {

  SettingTenant findAndInit(Long tenantId);

  SettingTenant find0(Long tenantId);

}
