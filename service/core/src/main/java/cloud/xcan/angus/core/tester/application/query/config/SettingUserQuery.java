package cloud.xcan.angus.core.tester.application.query.config;


import cloud.xcan.angus.core.tester.domain.config.user.UserSetting;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;

public interface SettingUserQuery {

  UserApiProxy findProxyByTenantId(Long optTenantId);

  UserSetting find(Long userId);

  UserSetting find0(Long userId);

}
