package cloud.xcan.angus.core.tester.application.cmd.config.impl;

import static cloud.xcan.angus.core.tester.application.converter.SettingUserConverter.initUserSetting;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.config.SettingUserCmd;
import cloud.xcan.angus.core.tester.application.query.config.SettingTenantQuery;
import cloud.xcan.angus.core.tester.application.query.config.SettingUserQuery;
import cloud.xcan.angus.core.tester.domain.config.tenant.SettingTenant;
import cloud.xcan.angus.core.tester.domain.config.user.SettingUser;
import cloud.xcan.angus.core.tester.domain.config.user.SettingUserRepo;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxyType;
import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.UserApiProxy;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of user setting command operations.
 * </p>
 * <p>
 * Manages user-specific settings including preferences, API proxy configurations, and social
 * binding information.
 * </p>
 * <p>
 * Provides user setting initialization, updates, and synchronization with tenant-level
 * configurations.
 * </p>
 */
@Biz
public class SettingUserCmdImpl extends CommCmd<SettingUser, Long> implements SettingUserCmd {

  @Resource
  private SettingUserRepo settingUserRepo;
  @Resource
  private SettingUserQuery settingUserQuery;
  @Resource
  private SettingTenantQuery settingTenantQuery;

  /**
   * <p>
   * Finds and initializes user settings.
   * </p>
   * <p>
   * Retrieves existing user settings or creates new ones with default values. Loads timezone
   * configuration and refreshes API proxy settings from tenant configuration.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public SettingUser findAndInit(Long userId) {
    return new BizTemplate<SettingUser>() {

      @Override
      protected SettingUser process() {
        SettingUser userSetting = settingUserQuery.find0(userId);
        SettingTenant tenantSetting = settingTenantQuery.findAndInit(getOptTenantId());
        if (nonNull(userSetting)) {
          // Load latest server proxy URL without persistence
          UserApiProxy apiProxy = userSetting.getApiProxy().copy();
          apiProxy.getServerProxy().setUrl(nonNull(tenantSetting.getServerApiProxyData())
              ? tenantSetting.getServerApiProxyData().getUrl() : null);
          userSetting.setApiProxyRefresh(apiProxy);
          return userSetting;
        }
        // Initialize new user settings
        userSetting = initUserSetting(getOptTenantId(), tenantSetting);
        userSetting.setId(userId);
        settingUserRepo.save(userSetting);
        return userSetting;
      }
    }.execute();
  }

  /**
   * <p>
   * Updates user API proxy URL configuration.
   * </p>
   * <p>
   * Updates the client proxy URL for the current user's API proxy settings.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void proxyUpdate(String url) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        SettingUser settingDb = settingUserQuery.find(getUserId());
        settingDb.getApiProxy().getClientProxy().setUrl(url);
        settingUserRepo.save(settingDb);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Enables specific API proxy type for user.
   * </p>
   * <p>
   * Activates the specified proxy type while disabling all other proxy types to ensure only one
   * proxy type is active at a time.
   * </p>
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void proxyEnabled(ApiProxyType proxyType) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        SettingUser settingDb = settingUserQuery.find(getUserId());
        switch (proxyType) {
          case NO_PROXY:
            // Enable no proxy, disable all others
            settingDb.getApiProxy().getNoProxy().setEnabled(true);
            settingDb.getApiProxy().getClientProxy().setEnabled(false);
            settingDb.getApiProxy().getServerProxy().setEnabled(false);
            settingDb.getApiProxy().getCloudProxy().setEnabled(false);
            break;
          case CLIENT_PROXY:
            // Enable client proxy, disable all others
            settingDb.getApiProxy().getNoProxy().setEnabled(false);
            settingDb.getApiProxy().getClientProxy().setEnabled(true);
            settingDb.getApiProxy().getServerProxy().setEnabled(false);
            settingDb.getApiProxy().getCloudProxy().setEnabled(false);
            break;
          case SERVER_PROXY:
            // Enable server proxy, disable all others
            settingDb.getApiProxy().getNoProxy().setEnabled(false);
            settingDb.getApiProxy().getClientProxy().setEnabled(false);
            settingDb.getApiProxy().getServerProxy().setEnabled(true);
            settingDb.getApiProxy().getCloudProxy().setEnabled(false);
            break;
          case CLOUD_PROXY:
            // Enable cloud proxy, disable all others
            settingDb.getApiProxy().getNoProxy().setEnabled(false);
            settingDb.getApiProxy().getClientProxy().setEnabled(false);
            settingDb.getApiProxy().getServerProxy().setEnabled(false);
            settingDb.getApiProxy().getCloudProxy().setEnabled(true);
            break;
          default:
            // NOOP for unsupported proxy types
        }
        settingUserRepo.save(settingDb);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<SettingUser, Long> getRepository() {
    return settingUserRepo;
  }
}
