package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.FuncTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.PerfTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.StabilityTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.TenantServerApiProxyTo;
import java.util.Objects;

public class TenantSettingAssembler {

  public static TenantServerApiProxyTo toServerApiProxyTo(ServerApiProxy data) {
    if (Objects.isNull(data)) {
      return null;
    }
    return new TenantServerApiProxyTo().setUrl(data.getUrl()).setEnabled(data.getEnabled());
  }

  public static ServerApiProxy updateToApiServerProxy(TenantServerApiProxyTo to) {
    return new ServerApiProxy()
        .setUrl(to.getUrl().startsWith("ws") ? to.getUrl() : "ws://" + to.getUrl())
        .setEnabled(to.getEnabled());
  }

  public static FuncData updateToFuncData(FuncTo func) {
    return new FuncData()
        .setSmoke(func.isSmoke())
        .setSmokeCheckSetting(func.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(func.getUserDefinedSmokeAssertion())
        .setSecurity(func.isSecurity())
        .setSecurityCheckSetting(func.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(func.getUserDefinedSecurityAssertion());
  }

  public static PerfData updateToPerf(PerfTo perf) {
    return new PerfData()
        .setThreads(perf.getThreads())
        .setRampUpThreads(perf.getRampUpThreads())
        .setRampUpInterval(perf.getRampUpInterval())
        .setDuration(perf.getDuration())
        .setArt(perf.getArt())
        .setPercentile(perf.getPercentile())
        .setTps(perf.getTps())
        .setErrorRate(perf.getErrorRate());
  }

  public static StabilityData updateToStability(StabilityTo stability) {
    return new StabilityData()
        .setThreads(stability.getThreads())
        .setDuration(stability.getDuration())
        .setArt(stability.getArt())
        .setPercentile(stability.getPercentile())
        .setTps(stability.getTps())
        .setErrorRate(stability.getErrorRate())
        .setCpu(stability.getCpu())
        .setDisk(stability.getDisk())
        .setMemory(stability.getMemory())
        .setNetwork(stability.getNetwork());
  }

  public static FuncTo toFuncTo(FuncData func) {
    return isEmpty(func) ? null
        : new FuncTo().setSmoke(func.isSmoke())
            .setSmokeCheckSetting(func.getSmokeCheckSetting())
            .setUserDefinedSmokeAssertion(func.getUserDefinedSmokeAssertion())
            .setSecurity(func.isSecurity())
            .setSecurityCheckSetting(func.getSecurityCheckSetting())
            .setUserDefinedSecurityAssertion(func.getUserDefinedSecurityAssertion());
  }

  public static PerfTo toPerfTo(PerfData perf) {
    return isEmpty(perf) ? null
        : new PerfTo().setThreads(perf.getThreads())
            .setRampUpThreads(perf.getRampUpThreads())
            .setRampUpInterval(perf.getRampUpInterval())
            .setDuration(perf.getDuration())
            .setArt(perf.getArt())
            .setPercentile(perf.getPercentile())
            .setTps(perf.getTps())
            .setErrorRate(perf.getErrorRate());
  }

  public static StabilityTo toStabilityTo(StabilityData stability) {
    return isEmpty(stability) ? null
        : new StabilityTo().setThreads(stability.getThreads())
            .setDuration(stability.getDuration())
            .setArt(stability.getArt())
            .setPercentile(stability.getPercentile())
            .setErrorRate(stability.getErrorRate())
            .setTps(stability.getTps())
            .setCpu(stability.getCpu())
            .setDisk(stability.getDisk())
            .setMemory(stability.getMemory())
            .setNetwork(stability.getNetwork());
  }


}
