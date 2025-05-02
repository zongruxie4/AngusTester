package cloud.xcan.angus.core.tester.infra.remoting;

import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.ALLOW_MAX_HEARTBEAT_INTERVAL;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REDIS_DATABASE;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REDIS_DEPLOY_MODE;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REDIS_HOST;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REDIS_PASSWORD;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REDIS_SENTINEL_MASTER_ID;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.REMOTING_CONFIG_ROOT;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.SEND_TIMEOUT;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.SERVER_IP;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.SERVER_PORT;
import static cloud.xcan.angus.remoting.common.config.RemotingConfigDefaults.SERVER_PUBLIC_IP;

import cloud.xcan.angus.remoting.common.MessageService;
import cloud.xcan.angus.remoting.server.RemotingServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(cloud.xcan.angus.core.tester.infra.remoting.RemotingServerProperties.class)
public class RemotingServerConfiguration {

  @Bean(destroyMethod = "shutdown")
  public RemotingServer remotingServer(
      cloud.xcan.angus.core.tester.infra.remoting.RemotingServerProperties properties) {
    configureRemotingServer(properties);
    RemotingServer server = new RemotingServer(MessageService.Ctrl,
        properties.getServerIp(), Integer.parseInt(properties.getServerPort()));
    try {
      log.info("\033[34m*** Start CtrlRemotingServer starting ***\033[0m");
      new Thread(server::start).start();
    } catch (Exception e) {
      log.error("*** Start CtrlRemotingServer failure and exit ***", e);
      server.shutdown();
    }
    return server;
  }

  private void configureRemotingServer(RemotingServerProperties prop) {
    if (prop.getServerIp() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + SERVER_IP, prop.getServerIp());
    }
    if (prop.getServerPublicIp() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + SERVER_PUBLIC_IP, prop.getServerPublicIp());
    } else {
      assert prop.getServerIp() != null;
      System.setProperty(REMOTING_CONFIG_ROOT + SERVER_PUBLIC_IP, prop.getServerIp());
    }
    if (prop.getServerPort() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + SERVER_PORT, prop.getServerPort());
    }
    if (prop.getAllowMaxHeartbeatInterval() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + ALLOW_MAX_HEARTBEAT_INTERVAL,
          prop.getAllowMaxHeartbeatInterval());
    }
    if (prop.getRedisDeployMode() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + REDIS_DEPLOY_MODE, prop.getRedisDeployMode());
    }
    if (prop.getRedisHost() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + REDIS_HOST, prop.getRedisHost());
    }
    if (prop.getRedisPassword() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + REDIS_PASSWORD, prop.getRedisPassword());
    }
    if (prop.getRedisSentinelMasterId() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + REDIS_SENTINEL_MASTER_ID,
          prop.getRedisSentinelMasterId());
    }
    if (prop.getRedisDatabase() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + REDIS_DATABASE, prop.getRedisDatabase());
    }
    if (prop.getSendTimeout() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + SEND_TIMEOUT, prop.getSendTimeout());
    }
  }
}
