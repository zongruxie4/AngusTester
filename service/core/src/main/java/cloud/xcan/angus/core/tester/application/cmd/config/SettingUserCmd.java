package cloud.xcan.angus.core.tester.application.cmd.config;


import cloud.xcan.angus.core.tester.domain.config.user.SettingUser;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxyType;

public interface SettingUserCmd {

  SettingUser findAndInit(Long userId);

  void proxyUpdate(String url);

  void proxyEnabled(ApiProxyType name);

}
