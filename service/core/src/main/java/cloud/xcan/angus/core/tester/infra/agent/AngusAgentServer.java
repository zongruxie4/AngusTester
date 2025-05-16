package cloud.xcan.angus.core.tester.infra.agent;

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

import cloud.xcan.angus.remoting.common.MessageService;
import cloud.xcan.angus.remoting.server.RemotingServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AngusAgentServer {

  public static RemotingServer start(AgentServerProperties properties) {
    configureRemotingServer(properties);
    RemotingServer server = new RemotingServer(MessageService.Ctrl,
        properties.getServerIp(), Integer.parseInt(properties.getServerPort()));
    try {
      log.info("\033[34m*** Start AngusAgentServer starting ***\033[0m");
      new Thread(server::start).start();
    } catch (Exception e) {
      log.error("*** Start AngusAgentServer failure and exit ***", e);
      server.shutdown();
    }
    return server;
  }

  private static void configureRemotingServer(AgentServerProperties prop) {
    if (prop.getServerIp() != null) {
      System.setProperty(REMOTING_CONFIG_ROOT + SERVER_IP, prop.getServerIp());
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
