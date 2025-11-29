package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.pojo.node.NodeInfo;
import cloud.xcan.angus.core.tester.domain.config.node.Node;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebugSource;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import java.util.LinkedHashMap;
import java.util.List;

public class ExecDebugConverter {

  public static ExecDebug startToDomain(ExecDebugSource source, Long id, Long scenarioId,
      Long scriptId, Long monitorId, ScriptType scriptType, Configuration configuration,
      Arguments arguments, Script script, AngusScript angusScript) {
    ExecDebug debug = new ExecDebug()
        .setId(id)
        .setName(script.getName())
        .setPlugin(script.getPlugin())
        .setSource(source)
        .setScriptType(scriptType)
        .setScriptId(scriptId)
        .setScenarioId(scenarioId)
        .setMonitorId(monitorId)
        .setStatus(ExecStatus.CREATED);
    Configuration config = nullSafe(configuration, angusScript.getConfiguration());
    if (debug.isMonitor()){
      config.setIterations(1L); // Force to iterate only once
    }
    debug.setAvailableNodeIds(nonNull(config.getNodeSelectors())
        ? config.getNodeSelectors().getAvailableNodeIds() : null)
        .setConfiguration(config)
        .setTask(angusScript.getTask());
    if (isNotEmpty(arguments)){
      debug.getTask().setArguments(arguments);
    }
    PipelineBuilder builder = PipelineBuilder.of(angusScript.getTask().getPipelines());
    if (nonNull(builder)) {
      LinkedHashMap<String, List<String>> pipelines = builder.getEnabledTargetNameMapping();
      debug.setSingleTargetPipeline(isEmpty(pipelines) /* Single task pipeline name can be empty*/
          || pipelines.size() == 1);
    }
    return debug;
  }

  public static NodeInfo toExecNodeInfo(Node node){
    return new NodeInfo().setId(node.getId())
        .setName(node.getName())
        .setIp(node.getIp())
        .setPublicIp(node.getPublicIp())
        .setDomain(node.getDomain())
        //.setRegionId(detailVo.getRegionId())
        .setSpec(node.getSpec());
  }

}
