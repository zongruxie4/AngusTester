package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;


import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyVo;

public class SettingUserAssembler {

  public static UserApiProxyVo toApiProxyVo(UserApiProxy apiProxy) {
    return new UserApiProxyVo().setNoProxy(apiProxy.getNoProxy())
        .setClientProxy(apiProxy.getClientProxy())
        .setServerProxy(apiProxy.getServerProxy())
        .setCloudProxy(apiProxy.getCloudProxy());
  }

}
