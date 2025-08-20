package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler;

import static cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter.isSingleTargetPipeline;
import static cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter.toSample;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;
import cloud.xcan.angus.model.SampleResult;
import cloud.xcan.angus.model.element.pipeline.PipelineBuilder;
import cloud.xcan.angus.model.meter.MeterStatus;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nullable;

public class ExecDebugAssembler {

  public static ExecDebug addDtoToDomain(ExecDebugStartDto dto) {
    ExecDebug debug = new ExecDebug()
        .setId(dto.getId())
        .setName(dto.getName())
        .setPlugin(dto.getPlugin())
        .setScriptType(ScriptType.TEST_FUNCTIONALITY)
        .setScriptId(null)
        .setStatus(ExecStatus.CREATED)
        .setAvailableNodeIds(nonNull(dto.getConfiguration().getNodeSelectors())
            ? dto.getConfiguration().getNodeSelectors().getAvailableNodeIds() : null)
        .setConfiguration(dto.getConfiguration())
        .setTask(dto.getTask());
    PipelineBuilder builder = PipelineBuilder.of(dto.getTask().getPipelines());
    if (nonNull(builder)) {
      LinkedHashMap<String, List<String>> pipelines = builder.getEnabledTargetNameMapping();
      debug.setSingleTargetPipeline(isEmpty(pipelines) /* Single task pipeline name can be empty*/
          || pipelines.size() == 1);
    }
    return debug;
  }

  public static ExecDebugDetailVo toDetailVo(@Nullable ExecDebug debug) {
    if (isNull(debug)) {
      return null;
    }
    ExecDebugDetailVo detailVo = new ExecDebugDetailVo()
        .setId(debug.getId())
        .setName(debug.getName())
        .setPlugin(debug.getPlugin())
        .setScriptType(debug.getScriptType())
        .setScriptId(debug.getScriptId())
        .setScenarioId(debug.getScenarioId())
        .setStatus(debug.getStatus())
        .setMessage(debug.getMessage())
        .setExecNode(debug.getExecNode())
        .setAvailableNodes(debug.getAvailableNodes())
        .setConfiguration(debug.getConfiguration())
        .setTask(debug.getTask())
        .setPipelineTargetMappings(debug.getPipelineTargetMappings())
        .setEndDate(debug.getEndDate())
        .setMeterStatus(debug.getMeterStatus())
        .setMeterMessage(debug.getMeterMessage())
        .setSingleTargetPipeline(isSingleTargetPipeline(debug.getPipelineTargetMappings()))
        .setCreatedBy(debug.getCreatedBy())
        .setCreatedDate(debug.getCreatedDate())
        .setSampleSummaryInfo(toSample(debug.getFinishSampleResult()))
        .setSchedulingResult(debug.getSchedulingResult());
    if (isNotEmpty(debug.getSampleContents())) {
      detailVo.setSampleContents(
          debug.getSampleContents().stream().map(ExecSampleConverter::toExecSampleContentInfo)
              .toList());
    }
    boolean testSucceed = debug.getStatus().isCompleted();
    String testFailureMessage = testSucceed ? null : debug.getMessage();
    if (testSucceed) {
      if (isNotEmpty(debug.getMeterStatus())
          && !MeterStatus.of(debug.getMeterStatus()).isSuccess()) {
        testSucceed = false;
        testFailureMessage = nullSafe(debug.getMeterMessage(), testFailureMessage);
      }
      if (testSucceed) {
        SampleResult<?> result = debug.getSampleContents().stream()
            .filter(x -> nonNull(x.getSampleResult()) && !x.getSampleResult().isSuccess())
            .map(ExecSampleContent::getSampleResult).findFirst().orElse(null);
        if (nonNull(result)){
          testSucceed = false;
          testFailureMessage = nullSafe(result.getFailMessage(), testFailureMessage);
        }
      }
    }
    detailVo.setTestSucceed(testSucceed).setTestFailureMessage(testFailureMessage);
    return detailVo;
  }
}
