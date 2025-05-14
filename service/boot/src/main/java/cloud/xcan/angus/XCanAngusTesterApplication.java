package cloud.xcan.angus;

import static cloud.xcan.angus.api.commonlink.TesterConstant.ENV_STARTUP_ANGUS_AGENT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.ENV_STARTUP_ANGUS_PROXY;
import static cloud.xcan.angus.core.spring.env.AngusEnvHelper.getBoolean;
import static cloud.xcan.angus.core.utils.CoreUtils.exitApp;

import cloud.xcan.angus.agent.XCanAngusAgent;
import cloud.xcan.angus.proxy.XCanAngusProxy;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients(basePackages = {
    "cloud.xcan.angus.api.gm",
    "cloud.xcan.angus.api.ess",
    "cloud.xcan.angus.api.storage",
    "cloud.xcan.angus.security"
})
@EnableDiscoveryClient
@SpringBootApplication
public class XCanAngusTesterApplication {

  public static void main(String[] args) {
    SpringApplication.run(XCanAngusTesterApplication.class, args);
  }

  @PostConstruct
  public void startAgent() {
    try {
      if (getBoolean(ENV_STARTUP_ANGUS_AGENT, false)) {
        XCanAngusAgent.start();
      }
    }catch (Exception e) {
      exitApp();
    }
  }

  @PostConstruct
  public void startProxy() {
    try {
      if (getBoolean(ENV_STARTUP_ANGUS_PROXY, false)) {
        XCanAngusProxy.start();
      }
    }catch (Exception e) {
      exitApp();
    }
  }
}
