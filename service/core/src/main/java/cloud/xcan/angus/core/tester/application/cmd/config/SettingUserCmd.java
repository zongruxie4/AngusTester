package cloud.xcan.angus.core.tester.application.cmd.config;


import cloud.xcan.angus.core.tester.domain.config.user.UserSetting;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxyType;

public interface SettingUserCmd {

  UserSetting findAndInit(Long userId);

  void proxyUpdate(String url);

  void proxyEnabled(ApiProxyType name);

}
