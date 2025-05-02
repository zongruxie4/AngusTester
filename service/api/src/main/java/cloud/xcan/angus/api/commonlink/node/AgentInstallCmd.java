package cloud.xcan.angus.api.commonlink.node;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AgentInstallCmd {

  /**
   * install-agent.sh
   */
  private String linuxInstallScriptName;

  /**
   * curl -s http://192.168.0.102:1805/filepxy/pubapi/v1/object/download/install-agent.sh?id=96204965327929444
   * -o install-agent.sh --retry 3 -m 120
   */
  private String linuxDownloadInstallScriptCmd;

  /**
   * ./install-agent.sh 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl'
   * a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
   */
  private String linuxRunInstallScriptCmd;

  /**
   * <pre>
   *
   * ./install-agent.sh 1.0.0 95883044644192280 http://localhost:6808/openapi2p/v1/ctrl 8294181a-9368-4447-8a96-a062ba8b9db2 1 1
   *
   * /bin/sh -c "$(curl -s http://192.168.0.102:1805/filepxy/pubapi/v1/object/download/install-agent.sh?id=96002032418160640) 1.0.0 95883044644192280 http://localhost:6808/openapi2p/v1/ctrl 8294181a-9368-4447-8a96-a062ba8b9db2 1 1"
   *
   * </pre>
   */
  private String linuxOnlineInstallCmd;

  /**
   * <pre>
   *  wget https://www.xcan.cloud/angus/install-agent-1.0.zip
   *  unzip install-agent-1.0.tar.gz -d install-agent-1.0
   *  cd install-agent-1.0
   *  /bin/sh install-agent.sh agrs...
   *
   *  Replace https://www.xcan.cloud/angus/install-agent-1.0.0.zip with the cloud upload file uri
   * </pre>
   */
  private Map<String, String> linuxOfflineInstallSteps;

  /**
   * install-agent.bat
   */
  private String windowsInstallScriptName;

  /**
   * $p = New-Object System.Net.WebClient $p.DownloadFile("https://www.xcan.cloud/angus/install-agent.bat"
   * "C:\Program Files\AngusAgent\")
   */
  private String windowsDownloadInstallScriptCmd;

  /**
   * <pre>
   *  $p = New-Object System.Net.WebClient
   *  $p.DownloadFile("https://www.xcan.cloud/angus/install-agent.bat" "C:\Program Files\AngusAgent\")
   *  cd C:\Program Files\AngusAgent\bin
   *  install-agent.bat install agrs...
   *  net start angusagent
   * </pre>
   */
  private String windowsOnlineInstallCmd;

  /**
   * ./install-agent.sh 1.0.0 99360265199419407 'http://localhost:6808/openapi2p/v1/ctrl'
   * a0694ac2-e5e7-4eda-a29d-afd447b6c5e5 1 1
   */
  private String windowsRunInstallScriptCmd;

  /**
   * <pre>
   *  $p = New-Object System.Net.WebClient
   *  $p.DownloadFile("https://www.xcan.cloud/angus/install-agent-1.0.0.zip" "C:\Program Files\AngusAgent-1.0.0\")
   *  cd C:\Program Files\AngusAgent\bin
   *  AngusAgent.exe install agrs...
   *  net start angusagent
   * </pre>
   */
  private Map<String, String> windowsOfflineInstallSteps;

  private NodeInstallConfig installConfig;
}
