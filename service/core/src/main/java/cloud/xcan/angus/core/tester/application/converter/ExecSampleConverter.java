package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleError;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleMergeBase;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScore;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummary;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThread;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughput;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class ExecSampleConverter {

  public static ExecSampleSummaryInfoVo toSample(ExecSample finishSample) {
    DecimalFormat format = new DecimalFormat("0.00");
    return isNull(finishSample) ? null : new ExecSampleSummaryInfoVo()
        .setFinish(finishSample.isFinish())
        .setName(finishSample.getName())
        .setDuration(finishSample.getDuration())
        .setStartTime(finishSample.getStartTime())
        .setEndTime(finishSample.getEndTime())
        .setErrors(finishSample.getErrors())
        .setIterations(finishSample.getIterations())
        .setN(finishSample.getN())
        .setOperations(finishSample.getOperations())
        .setTransactions(finishSample.getTransactions())
        .setReadBytes(finishSample.getReadBytes())
        .setWriteBytes(finishSample.getWriteBytes())
        .setOps(finishSample.getOps())
        .setTps(finishSample.getTps())
        .setBrps(finishSample.getBrps())
        .setBwps(finishSample.getBwps())
        .setTranMean(finishSample.getTranMean())
        .setTranMin(finishSample.getTranMin())
        .setTranMax(finishSample.getTranMax())
        .setTranP50(finishSample.getTranP50())
        .setTranP75(finishSample.getTranP75())
        .setTranP90(finishSample.getTranP90())
        .setTranP95(finishSample.getTranP95())
        .setTranP99(finishSample.getTranP99())
        .setTranP999(finishSample.getTranP999())
        .setErrorRate(finishSample.getErrorRate())
        //.setErrorCauseCounter(finishSample.getErrorCauseCounter())
        .setThreadPoolSize(finishSample.getThreadPoolSize())
        .setThreadPoolActiveSize(finishSample.getThreadPoolActiveSize())
        .setThreadMaxPoolSize(finishSample.getThreadMaxPoolSize())
        .setThreadRunning(finishSample.isThreadRunning())
        .setThreadTerminated(finishSample.isThreadTerminated())
        .setUploadResultBytes(finishSample.getUploadResultBytes())
        .setUploadResultTotalBytes(finishSample.getUploadResultTotalBytes())
        .setUploadResultProgress(finishSample.getUploadResultTotalBytes() > 0 ? format.format(
            (finishSample.getUploadResultBytes() / (double) finishSample
                .getUploadResultTotalBytes()) * 100) : null)
        //.setExtCounterMap1(finishSample.getExtCounterMap1())
        //.setExtCounterMap2(finishSample.getExtCounterMap2())
        .setExtCounter1(finishSample.getExtCounter1())
        .setExtCounter2(finishSample.getExtCounter2())
        .setExtGauge1(finishSample.getExtGauge1())
        .setExtGauge2(finishSample.getExtGauge2());
  }

  public static ExecSampleContent toExecSampleContentInfo(
      cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent sample) {
    return new ExecSampleContent()
        .setFinish(sample.isFinish())
        .setName(sample.getName())
        .setNodeId(sample.getNodeId())
        .setTimestamp0(sample.getTimestamp0())
        .setExtField(sample.getExtField())
        .setIteration(sample.getIteration())
        .setKey(sample.getKey())
        .setContent(sample.getSampleResult());
  }

  public static boolean isSingleTargetPipeline(
      LinkedHashMap<String, List<String>> pipelineTargetMappings) {
    if (isEmpty(pipelineTargetMappings)) {
      return true; // Default by web
    }
    if (pipelineTargetMappings.size() != 1) {
      return false;
    }
    return isEmpty(pipelineTargetMappings.values().stream().findFirst().get());
  }


  public static void mergeToFirst(ExecSampleMergeBase first, ExecSampleMergeBase other) {
    if (first instanceof ExecSample) {
      mergeToFirst((ExecSample) first, (ExecSample) other);
    }
    if (first instanceof ExecSampleSummary) {
      mergeToFirst((ExecSampleSummary) first, (ExecSampleSummary) other);
    }
    if (first instanceof ExecSampleScore) {
      mergeToFirst((ExecSampleScore) first, (ExecSampleScore) other);
    }
    if (first instanceof ExecSampleThroughput) {
      mergeToFirst((ExecSampleThroughput) first, (ExecSampleThroughput) other);
    }
    if (first instanceof ExecSampleThread) {
      mergeToFirst((ExecSampleThread) first, (ExecSampleThread) other);
    }
    if (first instanceof ExecSampleError) {
      mergeToFirst((ExecSampleError) first, (ExecSampleError) other);
    }
  }

  public static void mergeToFirst(ExecSample first, ExecSample other) {
    /**
     * =======Counter=======
     */
    first.setErrors(other.getErrors() > 0
        ? first.getErrors() + other.getErrors() : first.getErrors());
    first.setIterations(other.getIterations() > 0
        ? first.getIterations() + other.getIterations() : first.getIterations());
    first.setN(other.getN() > 0 ? first.getN() + other.getN() : first.getN());
    first.setOperations(other.getOperations() > 0
        ? first.getOperations() + other.getOperations() : first.getOperations());
    first.setTransactions(other.getTransactions() > 0
        ? first.getTransactions() + other.getTransactions() : first.getTransactions());
    first.setReadBytes(other.getReadBytes() > 0
        ? first.getReadBytes() + other.getReadBytes() : first.getReadBytes());
    first.setWriteBytes(other.getWriteBytes() > 0
        ? first.getWriteBytes() + other.getWriteBytes() : first.getWriteBytes());

    /**
     * ======Throughput======
     * <p>
     * Very target or target transaction group will be sampled
     */
    first.setOps(other.getOps() > 0 ? first.getOps() + other.getOps() : first.getOps());
    first.setTps(other.getTps() > 0 ? first.getTps() + other.getTps() : first.getTps());
    first.setBrps(other.getBrps() > 0 ? first.getBrps() + other.getBrps() : first.getBrps());
    first.setBwps(other.getBwps() > 0 ? first.getBwps() + other.getBwps() : first.getBwps());

    /**
     * ==Aggregation(Trans)=
     */
    first.setTranMean((first.getTranMean() + other.getTranMean()) / 2); // Note: Imprecise!
    first.setTranMin(Math.min(first.getTranMin(), other.getTranMin()));
    first.setTranMax(Math.max(first.getTranMax(), other.getTranMax()));

    /**
     * ==Percentile(Trans)==
     */
    first.setTranP50((first.getTranP50() + other.getTranP50()) / 2); // Note: Imprecise!
    first.setTranP75((first.getTranP75() + other.getTranP75()) / 2); // Note: Imprecise!
    first.setTranP90((first.getTranP90() + other.getTranP90()) / 2); // Note: Imprecise!
    first.setTranP95((first.getTranP95() + other.getTranP95()) / 2); // Note: Imprecise!
    first.setTranP99((first.getTranP99() + other.getTranP99()) / 2); // Note: Imprecise!
    first.setTranP999((first.getTranP999() + other.getTranP999()) / 2); // Note: Imprecise!

    /**
     * ========Error========
     */
    first.setErrorRate(first.getErrorRate() + other.getErrorRate());
    if (isNotEmpty(first.getErrorCauseCounter())) {
      if (isNotEmpty(other.getErrorCauseCounter())) {
        for (Entry<String, Long> entry : other.getErrorCauseCounter().entrySet()) {
          if (first.getErrorCauseCounter().containsKey(entry.getKey())) {
            first.getErrorCauseCounter().put(entry.getKey(), entry.getValue()
                + first.getErrorCauseCounter().get(entry.getKey()));
          } else {
            first.getErrorCauseCounter().put(entry.getKey(), entry.getValue());
          }
        }
      }
    } else {
      first.setErrorCauseCounter(other.getErrorCauseCounter());
    }

    /**
     * ========Threads=======
     */
    first.setThreadPoolSize(first.getThreadPoolSize() + other.getThreadPoolSize());
    first.setThreadPoolActiveSize(
        first.getThreadPoolActiveSize() + other.getThreadPoolActiveSize());
    first.setThreadMaxPoolSize(first.getThreadMaxPoolSize() + other.getThreadMaxPoolSize());
    // private boolean threadRunning;
    // private boolean threadTerminated;

    /**
     * ========UploadResultProgress=======
     */
    first.setUploadResultBytes(first.getUploadResultBytes() + other.getUploadResultBytes());
    first.setUploadResultTotalBytes(first.getUploadResultTotalBytes()
        + other.getUploadResultTotalBytes());

    /**
     * ========Extension====
     */
    if (isNotEmpty(first.getExtCounterMap1())) {
      if (isNotEmpty(other.getExtCounterMap1())) {
        for (Entry<String, Long> entry : other.getExtCounterMap1().entrySet()) {
          if (first.getExtCounterMap1().containsKey(entry.getKey())) {
            first.getExtCounterMap1().put(entry.getKey(), entry.getValue()
                + first.getExtCounterMap1().get(entry.getKey()));
          } else {
            first.getExtCounterMap1().put(entry.getKey(), entry.getValue());
          }
        }
      }
    } else {
      first.setExtCounterMap1(other.getExtCounterMap1());
    }
    if (isNotEmpty(first.getExtCounterMap2())) {
      if (isNotEmpty(other.getExtCounterMap2())) {
        for (Entry<String, Long> entry : other.getExtCounterMap2().entrySet()) {
          if (first.getExtCounterMap2().containsKey(entry.getKey())) {
            first.getExtCounterMap2().put(entry.getKey(), entry.getValue()
                + first.getExtCounterMap2().get(entry.getKey()));
          } else {
            first.getExtCounterMap2().put(entry.getKey(), entry.getValue());
          }
        }
      }
    } else {
      first.setExtCounterMap2(other.getExtCounterMap2());
    }

    first.setExtCounter1(first.getExtCounter1() + other.getExtCounter1());
    first.setExtCounter2(first.getExtCounter2() + other.getExtCounter2());
    first.setExtGauge1(first.getExtGauge1() + other.getExtGauge1());
    first.setExtGauge2(first.getExtGauge2() + other.getExtGauge2());
  }

  public static void mergeToFirst(ExecSampleSummary first, ExecSampleSummary other) {
    /**
     * =======Counter=======
     */
    first.setErrors(other.getErrors() > 0
        ? first.getErrors() + other.getErrors() : first.getErrors());
    first.setIterations(other.getIterations() > 0
        ? first.getIterations() + other.getIterations() : first.getIterations());
    first.setN(other.getN() > 0 ? first.getN() + other.getN() : first.getN());
    first.setOperations(other.getOperations() > 0
        ? first.getOperations() + other.getOperations() : first.getOperations());
    first.setTransactions(other.getTransactions() > 0
        ? first.getTransactions() + other.getTransactions() : first.getTransactions());
    first.setReadBytes(other.getReadBytes() > 0
        ? first.getReadBytes() + other.getReadBytes() : first.getReadBytes());
    first.setWriteBytes(other.getWriteBytes() > 0
        ? first.getWriteBytes() + other.getWriteBytes() : first.getWriteBytes());

    /**
     * ======Throughput======
     * <p>
     * Very target or target transaction group will be sampled
     */
    first.setOps(other.getOps() > 0 ? first.getOps() + other.getOps() : first.getOps());
    first.setTps(other.getTps() > 0 ? first.getTps() + other.getTps() : first.getTps());
    first.setBrps(other.getBrps() > 0 ? first.getBrps() + other.getBrps() : first.getBrps());
    first.setBwps(other.getBwps() > 0 ? first.getBwps() + other.getBwps() : first.getBwps());

    /**
     * ==Aggregation(Trans)=
     */
    first.setTranMean((first.getTranMean() + other.getTranMean()) / 2); // Note: Imprecise!
    first.setTranMin(Math.min(first.getTranMin(), other.getTranMin()));
    first.setTranMax(Math.max(first.getTranMax(), other.getTranMax()));

    /**
     * ==Percentile(Trans)==
     */
    first.setTranP50((first.getTranP50() + other.getTranP50()) / 2); // Note: Imprecise!
    first.setTranP75((first.getTranP75() + other.getTranP75()) / 2); // Note: Imprecise!
    first.setTranP90((first.getTranP90() + other.getTranP90()) / 2); // Note: Imprecise!
    first.setTranP95((first.getTranP95() + other.getTranP95()) / 2); // Note: Imprecise!
    first.setTranP99((first.getTranP99() + other.getTranP99()) / 2); // Note: Imprecise!
    first.setTranP999((first.getTranP999() + other.getTranP999()) / 2); // Note: Imprecise!

    /**
     * ========Error========
     */
    first.setErrorRate(first.getErrorRate() + other.getErrorRate());

    /**
     * ========Threads=======
     */
    first.setThreadPoolSize(first.getThreadPoolSize() + other.getThreadPoolSize());
    first.setThreadPoolActiveSize(
        first.getThreadPoolActiveSize() + other.getThreadPoolActiveSize());
    first.setThreadMaxPoolSize(first.getThreadMaxPoolSize() + other.getThreadMaxPoolSize());
    // private boolean threadRunning;
    // private boolean threadTerminated;

    /**
     * ========Extension====
     */
    first.setExtCounter1(first.getExtCounter1() + other.getExtCounter1());
    first.setExtCounter2(first.getExtCounter2() + other.getExtCounter2());
    first.setExtGauge1(first.getExtGauge1() + other.getExtGauge1());
    first.setExtGauge2(first.getExtGauge2() + other.getExtGauge2());
  }


  public static void mergeToFirst(ExecSampleScore first, ExecSampleScore other) {
    /**
     * ==Aggregation(Trans)=
     */
    first.setTranMean((first.getTranMean() + other.getTranMean()) / 2); // Note: Imprecise!
    first.setTranMin(Math.min(first.getTranMin(), other.getTranMin()));
    first.setTranMax(Math.max(first.getTranMax(), other.getTranMax()));

    /**
     * ==Percentile(Trans)==
     */
    first.setTranP50((first.getTranP50() + other.getTranP50()) / 2); // Note: Imprecise!
    first.setTranP75((first.getTranP75() + other.getTranP75()) / 2); // Note: Imprecise!
    first.setTranP90((first.getTranP90() + other.getTranP90()) / 2); // Note: Imprecise!
    first.setTranP95((first.getTranP95() + other.getTranP95()) / 2); // Note: Imprecise!
    first.setTranP99((first.getTranP99() + other.getTranP99()) / 2); // Note: Imprecise!
    first.setTranP999((first.getTranP999() + other.getTranP999()) / 2); // Note: Imprecise!
  }

  public static void mergeToFirst(ExecSampleThroughput first, ExecSampleThroughput other) {
    /**
     * ======Throughput======
     * <p>
     * Very target or target transaction group will be sampled
     */
    first.setOps(other.getOps() > 0 ? first.getOps() + other.getOps() : first.getOps());
    first.setTps(other.getTps() > 0 ? first.getTps() + other.getTps() : first.getTps());
    first.setBrps(other.getBrps() > 0 ? first.getBrps() + other.getBrps() : first.getBrps());
    first.setBwps(other.getBwps() > 0 ? first.getBwps() + other.getBwps() : first.getBwps());
  }

  public static void mergeToFirst(ExecSampleThread first, ExecSampleThread other) {
    /**
     * ========Threads=======
     */
    first.setThreadPoolSize(first.getThreadPoolSize() + other.getThreadPoolSize());
    first
        .setThreadPoolActiveSize(first.getThreadPoolActiveSize() + other.getThreadPoolActiveSize());
    first.setThreadMaxPoolSize(first.getThreadMaxPoolSize() + other.getThreadMaxPoolSize());
    first.setThreadRunning(first.isThreadRunning() || other.isThreadRunning());
    first.setThreadTerminated(first.isThreadTerminated() || other.isThreadTerminated());
  }

  public static void mergeToFirst(ExecSampleError first, ExecSampleError other) {
    /**
     * =======Counter=======
     */
    first.setErrors(other.getErrors() > 0
        ? first.getErrors() + other.getErrors() : first.getErrors());
    first.setN(other.getN() > 0 ? first.getN() + other.getN() : first.getN());
    first.setOperations(other.getOperations() > 0
        ? first.getOperations() + other.getOperations() : first.getOperations());
    first.setTransactions(other.getTransactions() > 0
        ? first.getTransactions() + other.getTransactions() : first.getTransactions());

    /**
     * ========Error========
     */
    first.setErrorRate(first.getErrorRate() + other.getErrorRate());
  }

}
