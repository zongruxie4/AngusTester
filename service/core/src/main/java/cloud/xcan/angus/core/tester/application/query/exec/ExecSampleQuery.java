package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleCounter;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleError;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScore;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummary;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThread;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughput;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleUploadResultProgress;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ExecSampleQuery {

  ExecSample summaryTotal(Long execId);

  Page<ExecSampleSummary> summaryList(Long execId,
      GenericSpecification<ExecSampleSummary> spec, PageRequest pageable);

  Page<ExecSampleScore> scoreList(Long execId,
      GenericSpecification<ExecSampleScore> spec, PageRequest pageable);

  Page<ExecSampleThroughput> throughputList(Long execId,
      GenericSpecification<ExecSampleThroughput> spec, PageRequest pageable);

  Page<ExecSampleThread> threadList(Long execId,
      GenericSpecification<ExecSampleThread> spec, PageRequest pageable);

  Page<ExecSampleError> errorList(Long execId,
      GenericSpecification<ExecSampleError> spec, PageRequest pageable);

  List<ExecSampleCounter> latestErrorsCounter(Long execId, Long nodeId, String name);

  ExecSampleUploadResultProgress latestUploadResultProgress(Long execId);

  List<ExecSampleCounter> latestExtCounterMap1(Long id, Long nodeId, String name);

  ExecSample getSingleTaskExecSampleResult(Long execId);

  ExecSample getSingleTaskExecSampleResult(Long execId, String name);

  ExecSample getSingleTaskExecSampleResult(Long execId, String name, int rampNum);

  void setExecInfoLatestTotalMergeSample(List<ExecInfo> execs);

  void setExecLatestTotalMergeSample(List<Exec> execs);

  void setExecInfoLatestTotalMergeSample(ExecInfo execInfo);

  ExecSample getExecLatestTotalMergeSample(ExecInfo execInfo);

  void setExecLatestTotalMergeSample(Exec exec);

  ExecSample getExecLatestTotalMergeSample(Exec exec);

  ExecSample getExecLatestMergeSample(Exec exec, String name);

  ExecSample getExecLatestMergeSample(Exec exec, String name, int rampNum);

  List<ExecSample> getExecLatestTotalMergeSampleByRampNum(Exec exec);

  List<ExecSample> getExecLatestMergeSampleByRampNum(Exec exec, String name);
}
