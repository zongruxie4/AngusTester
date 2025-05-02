package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.CriteriaUtils.findFirst;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getErrorContentSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getErrorSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getSampleExtcSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getScoreSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getSummarySpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getThreadSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.getThroughputSpecification;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toCounter;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toErrorListVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toScoreListVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toSummaryListVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toSummaryTotalVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toThreadListVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toThroughputListVo;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler.toUploadResultProgress;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_SAMPLE_RESULT_CONTENT;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.converter.ExecSampleConverter;
import cloud.xcan.angus.core.tester.application.query.exec.ExecQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleErrorContentQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleExtcQuery;
import cloud.xcan.angus.core.tester.application.query.exec.ExecSampleQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecSampleContentInfo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleCounter;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleError;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScore;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummary;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThread;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughput;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleUploadResultProgress;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecSampleFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleErrorContentFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleExtcFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecSampleAssembler;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecSampleErrorContentVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleUploadResultProgressVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ExecSampleFacadeImpl implements ExecSampleFacade {

  @Resource
  private ExecSampleQuery execSampleQuery;

  @Resource
  private ExecSampleErrorContentQuery execSampleErrorQuery;

  @Resource
  private ExecSampleExtcQuery execSampleExtcQuery;

  @Resource
  private ExecQuery execQuery;

  @Override
  public ExecSampleSummaryVo summaryTotal(Long id) {
    return toSummaryTotalVo(execSampleQuery.summaryTotal(id));
  }

  @Override
  public PageResult<ExecSampleVo> summaryList(Long id, ExecSampleFindDto dto) {
    Page<ExecSampleSummary> page = execSampleQuery
        .summaryList(id, getSummarySpecification(dto), dto.tranPage());
    if (nonNull(page) && page.hasContent()) {
      return PageResult.of(page.getTotalElements(), toSummaryListVo(page.getContent()));
    } else {
      return PageResult.empty();
    }
  }

  @Override
  public PageResult<ExecSampleVo> scoreList(Long id, ExecSampleFindDto dto) {
    Page<ExecSampleScore> page = execSampleQuery.scoreList(id,
        getScoreSpecification(dto), dto.tranPage());
    if (nonNull(page) && page.hasContent()) {
      return PageResult.of(page.getTotalElements(), toScoreListVo(page.getContent()));
    } else {
      return PageResult.empty();
    }
  }

  @Override
  public PageResult<ExecSampleVo> throughputList(Long id, ExecSampleFindDto dto) {
    Page<ExecSampleThroughput> page = execSampleQuery.throughputList(id,
        getThroughputSpecification(dto), dto.tranPage());
    if (nonNull(page) && page.hasContent()) {
      return PageResult.of(page.getTotalElements(), toThroughputListVo(page.getContent()));
    } else {
      return PageResult.empty();
    }
  }

  @Override
  public PageResult<ExecSampleVo> threadList(Long id, ExecSampleFindDto dto) {
    Page<ExecSampleThread> page = execSampleQuery.threadList(id,
        getThreadSpecification(dto), dto.tranPage());
    if (nonNull(page) && page.hasContent()) {
      return PageResult.of(page.getTotalElements(), toThreadListVo(page.getContent()));
    } else {
      return PageResult.empty();
    }
  }

  @Override
  public PageResult<ExecSampleVo> errorList(Long id, ExecSampleFindDto dto) {
    Page<ExecSampleError> page = execSampleQuery.errorList(id,
        getErrorSpecification(dto), dto.tranPage());
    if (nonNull(page) && page.hasContent()) {
      return PageResult.of(page.getTotalElements(), toErrorListVo(page.getContent()));
    } else {
      return PageResult.empty();
    }
  }

  @Override
  public PageResult<ExecSampleErrorContentVo> errorContent(Long id,
      ExecSampleErrorContentFindDto dto) {
    Page<ExecSampleErrorCause> page = execSampleErrorQuery.list(id,
        getErrorContentSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ExecSampleAssembler::toErrorContentListVo);
  }

  @Override
  public LinkedHashMap<String, LinkedHashMap<String, Long>> latestErrorsCounter(
      Long id, Long nodeId, String name) {
    LinkedHashMap<String, List<String>> pipelineTargetMappings
        = execQuery.getPipelineTargetMappings(id);
    List<ExecSampleCounter> errorCounters = execSampleQuery.latestErrorsCounter(id, nodeId, name);
    return toCounter(errorCounters, pipelineTargetMappings);
  }

  @Override
  public ExecSampleUploadResultProgressVo latestUploadResultProgress(Long id) {
    ExecSampleUploadResultProgress progress = execSampleQuery.latestUploadResultProgress(id);
    return toUploadResultProgress(progress);
  }

  @Override
  public LinkedHashMap<String, LinkedHashMap<String, Long>> latestExtCounterMap1(
      Long id, Long nodeId, String name) {
    LinkedHashMap<String, List<String>> pipelineTargetMappings = execQuery
        .getPipelineTargetMappings(id);
    List<ExecSampleCounter> extCounters = execSampleQuery.latestExtCounterMap1(id, nodeId, name);
    return toCounter(extCounters, pipelineTargetMappings);
  }

  @Override
  public PageResult<ExecSampleContentInfo> extContentList(Long id, ExecSampleExtcFindDto dto) {
    GenericSpecification<ExecSampleContent> spec = getSampleExtcSpecification(dto);
    SearchCriteria criteria = findFirst(spec.getCriteria(), "extField");
    boolean isSampleResultQuery = nonNull(criteria)
        && EXT_KEY_SAMPLE_RESULT_CONTENT.equals(criteria.getValue().toString());
    Page<ExecSampleContent> page = execSampleExtcQuery.list(id, spec, dto.tranPage());
    return buildVoPageResult(page, isSampleResultQuery
        ? ExecSampleConverter::toExecSampleContentInfo : ExecSampleAssembler::toExecSampleExtcVo);
  }

}
