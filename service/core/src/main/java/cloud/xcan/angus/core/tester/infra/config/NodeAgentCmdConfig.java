package cloud.xcan.angus.core.tester.infra.config;

import cloud.xcan.angus.core.tester.infra.config.NodeAgentCmdConfig.NodeAgentCmdProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NodeAgentCmdProperties.class)
public class NodeAgentCmdConfig {

  @Setter
  @Getter
  @ConfigurationProperties(prefix = "xcan.angus-agent", ignoreUnknownFields = false)
  public static final class NodeAgentCmdProperties {

    //agentVersion agentFileId serverCtrlUrlPrefix serverAccessToken tenantId deviceId
    private String agentVersion;
    private Long agentFileId;
    private String serverCtrlUrlPrefix;
    private String installScriptUrlPrefix;
    private String linuxInstallScriptId;
    private String linuxInstallScriptName;
    private String windowsInstallScriptId;
    private String windowsInstallScriptName;

  }

}
