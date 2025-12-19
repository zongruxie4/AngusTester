package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler;


import static cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter.isSingleTargetPipeline;
import static cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter.toSample;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecInfoVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecVo;

public class ExecAssembler {

  public static ExecInfoVo toExecInfoVo(ExecInfo exec) {
    return new ExecInfoVo().setId(exec.getId())
        .setProjectId(exec.getProjectId())
        .setName(exec.getName())
        .setPlugin(exec.getPlugin())
        .setPlatform(exec.getPlatform())
        .setScriptType(exec.getScriptType())
        .setScriptId(exec.getScriptId())
        //.setScriptSource(exec.getScriptSource())
        .setScriptSourceId(exec.getScriptSourceId())
        //.setScriptSourceName(exec.getScriptSourceName())
        .setStatus(exec.getStatus())
        .setIterations(exec.getIterations())
        .setDuration(exec.getDuration())
        .setThread(exec.getThread())
        .setPriority(exec.getPriority())
        .setIgnoreAssertions(exec.getIgnoreAssertions())
        .setStartMode(exec.getStartMode())
        .setStartAtDate(exec.getStartAtDate())
        .setReportInterval(exec.getReportInterval())
        .setUpdateTestResult(exec.getUpdateTestResult())
        .setCanUpdateTestResult(exec.canUpdateTestResult())
        .setSyncTestResult(exec.isSyncTestResult())
        .setAssocApiCaseIds(exec.getAssocApiCaseIds())
        .setTrial(exec.getTrial())
        .setActualStartDate(exec.getActualStartDate())
        .setEndDate(exec.getEndDate())
        .setMeterStatus(exec.getMeterStatus())
        .setMeterMessage(exec.getMeterMessage())
        .setSampleSummaryInfo(toSample(exec.getFinishSample()))
        .setSchedulingNum(exec.getSchedulingNum())
        .setLastSchedulingDate(exec.getLastSchedulingDate())
        .setLastSchedulingResult(exec.getLastSchedulingResult());
  }

  public static ExecDetailVo toExecDetailVo(Exec exec) {
    ExecDetailVo detailVo = new ExecDetailVo().setId(exec.getId())
        .setProjectId(exec.getProjectId())
        .setName(exec.getName())
        .setPlugin(exec.getPlugin())
        .setPlatform(exec.getPlatform())
        .setScriptType(exec.getScriptType())
        .setScriptId(exec.getScriptId())
        .setScriptName(exec.getScriptName())
        .setScriptSource(exec.getScriptSource())
        .setScriptSourceId(exec.getScriptSourceId())
        .setScriptSourceName(exec.getScriptSourceName())
        .setStatus(exec.getStatus())
        .setIterations(exec.getIterations())
        .setDuration(exec.getDuration())
        .setThread(exec.getThread())
        .setPriority(exec.getPriority())
        .setIgnoreAssertions(exec.getIgnoreAssertions())
        .setStartMode(exec.getStartMode())
        .setStartAtDate(exec.getStartAtDate())
        .setReportInterval(exec.getReportInterval())
        .setUpdateTestResult(exec.getUpdateTestResult())
        .setCanUpdateTestResult(exec.canUpdateTestResult())
        .setSyncTestResult(exec.isSyncTestResult())
        .setSyncTestResultFailure(exec.getSyncTestResultFailure())
        .setAssocApiCaseIds(exec.getAssocApiCaseIds())
        .setExecNodes(exec.getExecNodes())
        .setAvailableNodes(exec.getAvailableNodes())
        .setAppNodes(exec.getAppNodes())
        .setConfiguration(exec.getConfiguration())
        .setTask(exec.getTask())
        .setPipelineTargetMappings(exec.getPipelineTargetMappings())
        .setTrial(exec.getTrial())
        .setActualStartDate(exec.getActualStartDate())
        .setEndDate(exec.getEndDate())
        .setMeterStatus(exec.getMeterStatus())
        .setMeterMessage(exec.getMeterMessage())
        .setSingleTargetPipeline(isSingleTargetPipeline(exec.getPipelineTargetMappings()))
        .setExecBy(exec.getExecBy())
        .setTenantId(exec.getTenantId())
        .setCreatedBy(exec.getCreatedBy())
        .setCreatedDate(exec.getCreatedDate())
        .setModifiedBy(exec.getModifiedBy())
        .setModifiedDate(exec.getModifiedDate())
        .setSampleSummaryInfo(toSample(exec.getFinishSampleResult()))
        .setHasOperationPermission(exec.isHasOperationPermission())
        .setSchedulingNum(exec.getSchedulingNum())
        .setLastSchedulingDate(exec.getLastSchedulingDate())
        .setLastSchedulingResult(exec.getLastSchedulingResult());
    if (isNotEmpty(exec.getSampleContents())) {
      detailVo.setSampleContents(exec.getSampleContents().stream()
          .map(ExecSampleAssembler::toExecSampleExtcVo).toList());
    }
    return detailVo;
  }

  public static ExecVo toExecVo(ExecInfo exec) {
    return new ExecVo().setId(exec.getId())
        .setProjectId(exec.getProjectId())
        .setName(exec.getName())
        .setPlatform(exec.getPlatform())
        .setPlugin(exec.getPlugin())
        .setScriptType(exec.getScriptType())
        .setScriptId(exec.getScriptId())
        .setScriptName(exec.getScriptName())
        .setScriptSource(exec.getScriptSource())
        .setScriptSourceId(exec.getScriptSourceId())
        .setScriptSourceName(exec.getScriptSourceName())
        .setStatus(exec.getStatus())
        .setIterations(exec.getIterations())
        .setDuration(exec.getDuration())
        .setThread(exec.getThread())
        .setPriority(exec.getPriority())
        .setIgnoreAssertions(exec.getIgnoreAssertions())
        .setStartMode(exec.getStartMode())
        .setStartAtDate(exec.getStartAtDate())
        .setReportInterval(exec.getReportInterval())
        .setUpdateTestResult(exec.getUpdateTestResult())
        .setCanUpdateTestResult(exec.canUpdateTestResult())
        .setSyncTestResult(exec.isSyncTestResult())
        .setSyncTestResultFailure(exec.getSyncTestResultFailure())
        .setAssocApiCaseIds(exec.getAssocApiCaseIds())
        .setTrial(exec.getTrial())
        .setActualStartDate(exec.getActualStartDate())
        .setEndDate(exec.getEndDate())
        .setMeterStatus(exec.getMeterStatus())
        .setMeterMessage(exec.getMeterMessage())
        .setSingleTargetPipeline(exec.getSingleTargetPipeline())
        .setExecBy(exec.getExecBy())
        .setTenantId(exec.getTenantId())
        .setCreatedBy(exec.getCreatedBy())
        .setCreatedDate(exec.getCreatedDate())
        .setModifiedBy(exec.getModifiedBy())
        .setModifiedDate(exec.getModifiedDate())
        .setSampleSummaryInfo(toSample(exec.getFinishSample()))
        .setHasOperationPermission(exec.isHasOperationPermission())
        .setSchedulingNum(exec.getSchedulingNum())
        .setLastSchedulingDate(exec.getLastSchedulingDate())
        .setLastSchedulingResult(exec.getLastSchedulingResult());
  }

  public static GenericSpecification<ExecInfo> getSpecification(ExecFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "plugin", "extSearchMerge")
        .rangeSearchFields("id", "createdDate", "modifiedDate", "startDate",
            "actualStartDate", "endDate", "priority")
        .inAndNotFields("id", "name", "plugin", "platform", "scriptType", "priority", "status")
        .orderByFields("id", "createdDate", "startDate", "modifiedDate", "actualStartDate",
            "endDate", "priority")
        .build());
  }

}
