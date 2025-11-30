package cloud.xcan.angus.core.tester.interfaces.config.facade.internal;


import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.toFuncTo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.toPerfTo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.toServerApiProxyTo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.toStabilityTo;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.updateToApiServerProxy;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.updateToFuncData;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.updateToPerf;
import static cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler.TenantSettingAssembler.updateToStability;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;

import cloud.xcan.angus.core.tester.application.cmd.config.SettingTenantCmd;
import cloud.xcan.angus.core.tester.application.query.config.SettingTenantQuery;
import cloud.xcan.angus.core.tester.domain.config.tenant.event.TesterEvent;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import cloud.xcan.angus.core.tester.interfaces.config.facade.SettingTenantFacade;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.FuncTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.PerfTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.StabilityTo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.to.TenantServerApiProxyTo;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SettingTenantFacadeImpl implements SettingTenantFacade {

  @Resource
  private SettingTenantQuery settingTenantQuery;

  @Resource
  private SettingTenantCmd settingTenantCmd;

  @Override
  public void proxyReplace(TenantServerApiProxyTo dto) {
    settingTenantCmd.proxyReplace(updateToApiServerProxy(dto));
  }

  @Override
  public TenantServerApiProxyTo proxyDetail() {
    return toServerApiProxyTo(settingTenantQuery.findAndInit(getOptTenantId()).getServerApiProxyData());
  }

  @Override
  public void testerEventReplace(List<TesterEvent> dto) {
    settingTenantCmd.testerEventReplace(dto);
  }

  @Override
  public List<TesterEvent> testerEventDetail() {
    return settingTenantQuery.findAndInit(getOptTenantId()).getTesterEventData();
  }

  @Override
  public void evaluationReplace(LinkedHashMap<EvaluationPurpose, Integer> evaluation) {
    settingTenantCmd.evaluationReplace(evaluation);
  }

  @Override
  public LinkedHashMap<EvaluationPurpose, Integer> evaluationDetail() {
    return settingTenantQuery.findAndInit(getOptTenantId()).getEvaluationWeightData();
  }

  @Override
  public void funcReplace(FuncTo dto) {
    settingTenantCmd.funcReplace(updateToFuncData(dto));
  }

  @Override
  public FuncTo funcDetail() {
    return toFuncTo(settingTenantQuery.findAndInit(getOptTenantId()).getFuncData());
  }

  @Override
  public void perfReplace(PerfTo dto) {
    settingTenantCmd.perfReplace(updateToPerf(dto));
  }

  @Override
  public PerfTo perfDetail() {
    return toPerfTo(settingTenantQuery.findAndInit(getOptTenantId()).getPerfData());
  }

  @Override
  public void stabilityReplace(StabilityTo dto) {
    settingTenantCmd.stabilityReplace(updateToStability(dto));
  }

  @Override
  public StabilityTo stabilityDetail() {
    return toStabilityTo(settingTenantQuery.findAndInit(getOptTenantId()).getStabilityData());
  }
}
