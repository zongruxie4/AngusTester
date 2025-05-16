package cloud.xcan.angus.config;

import static cloud.xcan.angus.core.spring.env.EnvHelper.getBoolean;
import static cloud.xcan.angus.core.spring.env.EnvKeys.AGENT_STARTUP_IN_TESTER;
import static cloud.xcan.angus.core.spring.env.EnvKeys.PROXY_STARTUP_IN_TESTER;
import static cloud.xcan.angus.core.utils.CoreUtils.exitApp;

import cloud.xcan.angus.agent.AngusAgent;
import cloud.xcan.angus.core.tester.application.cmd.node.NodeInfoCmd;
import cloud.xcan.angus.core.tester.infra.agent.AgentServerProperties;
import cloud.xcan.angus.core.tester.infra.agent.AngusAgentServer;
import cloud.xcan.angus.proxy.AngusProxy;
import cloud.xcan.angus.remoting.server.RemotingServer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AgentServerProperties.class)
public class ServiceConfig {

  @Bean(destroyMethod = "shutdown")
  public RemotingServer remotingServer(AgentServerProperties properties) {
    return AngusAgentServer.start(properties);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startAgent(NodeInfoCmd nodeInfoCmd) {
    try {
      if (getBoolean(AGENT_STARTUP_IN_TESTER, false)) {
        nodeInfoCmd.configureAgentAuth();
        AngusAgent.start();
      }
    } catch (Exception e) {
      exitApp();
    }
  }

  @EventListener(ApplicationReadyEvent.class)
  public void startProxy() {
    try {
      if (getBoolean(PROXY_STARTUP_IN_TESTER, false)) {
        AngusProxy.start();
      }
    } catch (Exception e) {
      exitApp();
    }
  }

}
