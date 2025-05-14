package cloud.xcan.angus.core.tester.infra.remoting;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.remoting", ignoreUnknownFields = false)
public class RemotingServerProperties {

  private String serverIp;
  private String serverPort = "5035";

  /**
   * The client heartbeat frequency allowed by the server, in milliseconds
   */
  private String allowMaxHeartbeatInterval = "30000";
  private String sendTimeout = "60000";

  /**
   * Available values: Standalone, Sentinel, Cluster
   */
  private String redisDeployMode;
  private String redisHost;
  private String redisPassword;
  private String redisSentinelMasterId;
  private String redisDatabase = "2";

}
