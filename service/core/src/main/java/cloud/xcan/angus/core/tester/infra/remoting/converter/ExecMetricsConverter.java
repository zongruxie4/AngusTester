package cloud.xcan.angus.core.tester.infra.remoting.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_CONTENT1;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_CONTENT2;
import static cloud.xcan.angus.metrics.ExecMetrics.EXT_KEY_SAMPLE_RESULT_CONTENT;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.metrics.ExecMetrics;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ExecMetricsConverter {

  public static List<ExecSample> toExecMetrics(
      List<ExecMetrics> sampleMetrics, Long tenantId, Long deviceId,
      Long execId, Long now, boolean finish) {
    List<ExecSample> execMetrics = new ArrayList<>();
    for (ExecMetrics sample : sampleMetrics) {
      ExecSample metrics = new ExecSample();
      metrics.setTenantId(tenantId);
      metrics.setId(getCachedUidGenerator().getUID())
          .setExecId(execId)
          .setNodeId(deviceId)
          .setFinish(finish)
          .setTimestamp(now)
          .setTimestamp0(sample.getTimestamp())
          .setName(sample.getName())
          .setRampNum(sample.getRampNum())
          .setDuration(sample.getDuration())
          .setDuration0(sample.getDuration0())
          .setStartTime(sample.getStartTime())
          .setEndTime(sample.getEndTime())
          .setErrors(sample.getErrors())
          .setIterations(sample.getIterations())
          .setN(sample.getN())
          .setOperations(sample.getOperations())
          .setTransactions(sample.getTransactions())
          .setReadBytes(sample.getReadBytes())
          .setWriteBytes(sample.getWriteBytes())
          .setOps(sample.getOps())
          .setTps(sample.getTps())
          .setBrps(sample.getBrps())
          .setBwps(sample.getBwps())
          .setTranMean(sample.getTranMean())
          .setTranMin(sample.getTranMin())
          .setTranMax(sample.getTranMax())
          .setTranP50(sample.getTranP50())
          .setTranP75(sample.getTranP75())
          .setTranP90(sample.getTranP90())
          .setTranP95(sample.getTranP95())
          .setTranP99(sample.getTranP99())
          .setTranP999(sample.getTranP999())
          .setErrorRate(sample.getErrorRate0())
          .setErrorCauseCounter(sample.getErrorCauseCounter())
          .setThreadPoolSize(sample.getThreadPoolSize())
          .setThreadPoolActiveSize(sample.getThreadPoolActiveSize())
          .setThreadMaxPoolSize(sample.getThreadMaxPoolSize())
          .setThreadRunning(sample.isThreadRunning())
          .setThreadTerminated(sample.isThreadTerminated())
          .setUploadResultBytes(sample.getUploadResultBytes())
          .setUploadResultTotalBytes(sample.getUploadResultTotalBytes())
          .setExtCounterMap1(sample.getExtCounterMap1())
          .setExtCounterMap2(sample.getExtCounterMap2())
          .setExtCounter1(sample.getExtCounter1())
          .setExtCounter2(sample.getExtCounter2())
          .setExtGauge1(sample.getExtGauge1())
          .setExtGauge2(sample.getExtGauge2());
      execMetrics.add(metrics);
    }
    return execMetrics;
  }

  public static List<ExecSampleErrorCause> toExecSampleErrorCause(
      List<ExecMetrics> sampleMetrics, Long tenantId, Long deviceId, Long execId, Long now,
      boolean finish) {
    List<ExecSampleErrorCause> execMetrics = new ArrayList<>();
    for (ExecMetrics sample : sampleMetrics) {
      if (ObjectUtils.isNotEmpty(sample.getErrorCause())) {
        for (Map<String, String> content : sample.getErrorCause()) {
          ExecSampleErrorCause metrics = new ExecSampleErrorCause();
          Entry<String, String> entry = content.entrySet().stream().findFirst().orElse(null);
          if (nonNull(entry)) {
            metrics.setTenantId(tenantId);
            metrics.setExecId(execId)
                .setNodeId(deviceId)
                .setFinish(finish)
                .setTimestamp(now)
                .setTimestamp0(sample.getTimestamp())
                .setName(sample.getName())
                .setKey(entry.getKey())
                .setContent(entry.getValue());
            execMetrics.add(metrics);
          }
        }
      }
    }

    // Sort by sampling frequency
    execMetrics.sort(Comparator.comparing(x -> Long.valueOf(x.getKey())));
    for (ExecSampleErrorCause execMetric : execMetrics) {
      execMetric.setId(getCachedUidGenerator().getUID());
    }
    return execMetrics;
  }

  public static List<ExecSampleContent> toExecSampleContent(
      List<ExecMetrics> sampleMetrics, Long tenantId, Long deviceId,
      Long execId, Long now, boolean finish) {
    List<ExecSampleContent> execMetrics = new ArrayList<>();
    for (ExecMetrics sample : sampleMetrics) {
      if (ObjectUtils.isNotEmpty(sample.getSampleResultContent())) {
        execMetrics.addAll(sample.getSampleResultContent().entrySet().stream().map(x -> {
          ExecSampleContent metrics = new ExecSampleContent();
          metrics.setTenantId(tenantId);
          String[] keys = x.getKey().split("-");
          Object realName = ((LinkedHashMap) x.getValue()).get("name");
          metrics.setExecId(execId)
              .setNodeId(deviceId)
              .setFinish(finish)
              .setTimestamp(now)
              .setTimestamp0(sample.getTimestamp())
              .setName(nonNull(realName) ? realName.toString() : sample.getName())
              .setExtField(EXT_KEY_SAMPLE_RESULT_CONTENT)
              .setIteration(Long.valueOf(keys[0]))
              .setKey(keys[1])
              .setContent(nonNull(x.getValue()) ? JsonUtils.toJson(x.getValue()) : null);
          return metrics;
        }).collect(Collectors.toList()));
      }

      if (ObjectUtils.isNotEmpty(sample.getExtContent1())) {
        execMetrics.addAll(sample.getExtContent1().entrySet().stream().map(x -> {
          ExecSampleContent metrics = new ExecSampleContent();
          metrics.setTenantId(tenantId);
          String[] keys = x.getKey().split("-");
          metrics.setExecId(execId)
              .setNodeId(deviceId)
              .setFinish(finish)
              .setTimestamp(now)
              .setTimestamp0(sample.getTimestamp())
              .setName(sample.getName())
              .setExtField(EXT_KEY_CONTENT1)
              .setIteration(Long.valueOf(keys[0]))
              .setKey(keys[1])
              .setContent(nonNull(x.getValue()) ? JsonUtils.toJson(x.getValue()) : null);
          return metrics;
        }).collect(Collectors.toList()));
      }

      if (ObjectUtils.isNotEmpty(sample.getExtContent2())) {
        execMetrics.addAll(sample.getExtContent2().entrySet().stream().map(x -> {
          ExecSampleContent metrics = new ExecSampleContent();
          metrics.setTenantId(tenantId);
          String[] keys = x.getKey().split("-");
          Object realName = ((LinkedHashMap) x.getValue()).get("name");
          metrics.setExecId(execId)
              .setNodeId(deviceId)
              .setFinish(finish)
              .setTimestamp(now)
              .setTimestamp0(sample.getTimestamp())
              .setName(nonNull(realName) ? realName.toString() : sample.getName())
              .setExtField(EXT_KEY_CONTENT2)
              .setIteration(Long.valueOf(keys[0]))
              .setKey(keys[1])
              .setContent(nonNull(x.getValue()) ? JsonUtils.toJson(x.getValue()) : null);
          return metrics;
        }).collect(Collectors.toList()));
      }
    }

    // Sort by sampling frequency
    execMetrics.sort(Comparator.comparing(x -> Long.valueOf(x.getKey())));
    for (ExecSampleContent execMetric : execMetrics) {
      execMetric.setId(getCachedUidGenerator().getUID());
    }
    return execMetrics;
  }
}
