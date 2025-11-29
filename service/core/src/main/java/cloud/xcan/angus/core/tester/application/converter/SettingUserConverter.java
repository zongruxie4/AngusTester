package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_CLOUD_API_PROXY;
import static cloud.xcan.angus.api.commonlink.TesterConstant.DEFAULT_LOCAL_API_PROXY;

import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;
import cloud.xcan.angus.core.tester.domain.config.user.SettingUser;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxy;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxyType;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import java.util.Objects;

public class SettingUserConverter {

  public static SettingUser initUserSetting(Long tenantId, SettingTenant tenantSetting) {
    SettingUser user = new SettingUser();
    user.setTenantId(tenantId);

    UserApiProxy apiProxyData = assembleProxyConfig(tenantSetting);
    user.setApiProxy(apiProxyData);
    return user;
  }

  public static UserApiProxy assembleProxyConfig(SettingTenant tenantSetting) {
    return new UserApiProxy()
        .setNoProxy(new ApiProxy()
            .setName(ApiProxyType.NO_PROXY)
            .setEnabled(false)
            .setUrl(null))
        .setClientProxy(new ApiProxy()
            .setName(ApiProxyType.CLIENT_PROXY)
            .setEnabled(false)
            .setUrl(DEFAULT_LOCAL_API_PROXY))
        .setServerProxy(new ApiProxy()
            .setName(ApiProxyType.SERVER_PROXY)
            .setEnabled(Objects.nonNull(tenantSetting.getServerApiProxyData()))
            .setUrl(Objects.nonNull(tenantSetting.getServerApiProxyData()) ? tenantSetting
                .getServerApiProxyData().getUrl() : null))
        .setCloudProxy(new ApiProxy()
            .setName(ApiProxyType.CLOUD_PROXY)
            .setEnabled(Objects.isNull(tenantSetting.getServerApiProxyData()))
            .setUrl(DEFAULT_CLOUD_API_PROXY)
        );
  }

}
