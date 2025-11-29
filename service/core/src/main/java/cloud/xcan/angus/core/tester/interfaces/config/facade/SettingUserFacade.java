package cloud.xcan.angus.core.tester.interfaces.config.facade;


import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiClientProxyUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyEnabledDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyVo;

public interface SettingUserFacade {

  void proxyUpdate(UserApiClientProxyUpdateDto dto);

  void proxyEnabled(UserApiProxyEnabledDto dto);

  UserApiProxyVo proxyDetail();

}
