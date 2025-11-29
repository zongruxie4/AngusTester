package cloud.xcan.angus.core.tester.application.query.config;


import cloud.xcan.angus.core.tester.domain.config.user.SettingUser;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;

public interface SettingUserQuery {

  UserApiProxy findProxyByTenantId(Long optTenantId);

  SettingUser find(Long userId);

  SettingUser find0(Long userId);

}
