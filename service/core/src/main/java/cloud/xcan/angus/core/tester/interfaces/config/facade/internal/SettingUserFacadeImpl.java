package cloud.xcan.angus.core.tester.interfaces.config.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.SettingUserAssembler.toApiProxyVo;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.tester.application.cmd.config.SettingUserCmd;
import cloud.xcan.angus.core.tester.application.query.config.SettingUserQuery;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import cloud.xcan.angus.core.tester.interfaces.config.facade.SettingUserFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiClientProxyUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyEnabledDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting.UserApiProxyVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class SettingUserFacadeImpl implements SettingUserFacade {

  @Resource
  private SettingUserQuery settingUserQuery;

  @Resource
  private SettingUserCmd settingUserCmd;

  @Override
  public void proxyUpdate(UserApiClientProxyUpdateDto dto) {
    settingUserCmd.proxyUpdate(dto.getUrl());
  }

  @Override
  public void proxyEnabled(UserApiProxyEnabledDto dto) {
    settingUserCmd.proxyEnabled(dto.getName());
  }

  @Override
  public UserApiProxyVo proxyDetail() {
    UserApiProxy proxy = settingUserCmd.findAndInit(getUserId()).getApiProxyRefresh();
    return toApiProxyVo(proxy);
  }

}
