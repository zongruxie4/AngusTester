package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecSampleContentInfo;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleCounter;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleError;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleScore;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleSummary;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThread;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleThroughput;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleUploadResultProgress;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleErrorContentFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleExtcFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecSampleErrorContentVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleUploadResultProgressVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleVo.ScoreValue;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class ExecSampleAssembler {

  public static ExecSampleSummaryVo toSummaryTotalVo(ExecSample finishSample) {
    return isNull(finishSample) ? null : new ExecSampleSummaryVo()
        .setFinish(finishSample.isFinish())
        .setTimestamp00(finishSample.getTimestamp())
        .setIterations0(finishSample.getIterations0())
        .setDuration0(finishSample.getDurationUnit())
        .setTimestamp00(finishSample.getTimestamp0())
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
        .setUploadResultProgress(finishSample.getUploadResultProgress())
        //.setExtCounterMap1(finishSample.getExtCounterMap1())
        //.setExtCounterMap2(finishSample.getExtCounterMap2())
        .setExtCounter1(finishSample.getExtCounter1())
        .setExtCounter2(finishSample.getExtCounter2())
        .setExtGauge1(finishSample.getExtGauge1())
        .setExtGauge2(finishSample.getExtGauge2());
  }

  public static List<ExecSampleVo> toSummaryListVo(List<ExecSampleSummary> samples) {
    Map<Long, Map<String, ExecSampleSummary/*List<ExecSampleSummary>*/>>
        timestampAndNameGroup = samples.stream().collect(
        Collectors.groupingBy(ExecSampleSummary::getTimestamp,
            Collectors.toMap(ExecSampleSummary::getName, x -> x)
            /*Collectors.groupingBy(ExecSampleSummary::getName)*/));
    List<ExecSampleVo> vos = new ArrayList<>();
    for (Entry<Long, Map<String, ExecSampleSummary>> dateMapEntry : timestampAndNameGroup
        .entrySet()) {
      ExecSampleVo vo = new ExecSampleVo();
      vo.setTimestamp0(dateMapEntry.getKey());
      List<ScoreValue> values = new ArrayList<>();
      for (Entry<String, ExecSampleSummary> entry : dateMapEntry.getValue().entrySet()) {
        values.add(new ScoreValue().setName(entry.getKey())
            .setCvsValue(entry.getValue().toCsvStringValue()));
      }
      vo.setValues(values);
      vos.add(vo);
    }
    vos.sort(Comparator.comparing(ExecSampleVo::getTimestamp));
    return vos;
  }

  public static List<ExecSampleVo> toScoreListVo(List<ExecSampleScore> samples) {
    Map<Long, Map<String, ExecSampleScore/*List<ExecSampleSummary>*/>>
        timestampAndNameGroup = samples.stream().collect(
        Collectors.groupingBy(ExecSampleScore::getTimestamp,
            Collectors.toMap(ExecSampleScore::getName, x -> x)
            /*Collectors.groupingBy(ExecSampleSummary::getName)*/));
    List<ExecSampleVo> vos = new ArrayList<>();
    for (Entry<Long, Map<String, ExecSampleScore>> dateMapEntry : timestampAndNameGroup
        .entrySet()) {
      ExecSampleVo vo = new ExecSampleVo();
      vo.setTimestamp0(dateMapEntry.getKey());
      List<ScoreValue> values = new ArrayList<>();
      for (Entry<String, ExecSampleScore> entry : dateMapEntry.getValue().entrySet()) {
        values.add(new ScoreValue().setName(entry.getKey())
            .setCvsValue(entry.getValue().toCsvStringValue()));
      }
      vo.setValues(values);
      vos.add(vo);
    }
    vos.sort(Comparator.comparing(ExecSampleVo::getTimestamp));
    return vos;
  }

  public static List<ExecSampleVo> toThroughputListVo(List<ExecSampleThroughput> samples) {
    Map<Long, Map<String, ExecSampleThroughput/*List<ExecSampleSummary>*/>>
        timestampAndNameGroup = samples.stream().collect(
        Collectors.groupingBy(ExecSampleThroughput::getTimestamp,
            Collectors.toMap(ExecSampleThroughput::getName, x -> x)
            /*Collectors.groupingBy(ExecSampleSummary::getName)*/));
    List<ExecSampleVo> vos = new ArrayList<>();
    for (Entry<Long, Map<String, ExecSampleThroughput>> dateMapEntry : timestampAndNameGroup
        .entrySet()) {
      ExecSampleVo vo = new ExecSampleVo();
      vo.setTimestamp0(dateMapEntry.getKey());
      List<ScoreValue> values = new ArrayList<>();
      for (Entry<String, ExecSampleThroughput> entry : dateMapEntry.getValue().entrySet()) {
        values.add(new ScoreValue().setName(entry.getKey())
            .setCvsValue(entry.getValue().toCsvStringValue()));
      }
      vo.setValues(values);
      vos.add(vo);
    }
    vos.sort(Comparator.comparing(ExecSampleVo::getTimestamp));
    return vos;
  }

  public static List<ExecSampleVo> toThreadListVo(List<ExecSampleThread> samples) {
    Map<Long, Map<String, ExecSampleThread/*List<ExecSampleSummary>*/>>
        timestampAndNameGroup = samples.stream().collect(
        Collectors.groupingBy(ExecSampleThread::getTimestamp,
            Collectors.toMap(ExecSampleThread::getName, x -> x)
            /*Collectors.groupingBy(ExecSampleSummary::getName)*/));
    List<ExecSampleVo> vos = new ArrayList<>();
    for (Entry<Long, Map<String, ExecSampleThread>> dateMapEntry : timestampAndNameGroup
        .entrySet()) {
      ExecSampleVo vo = new ExecSampleVo();
      vo.setTimestamp0(dateMapEntry.getKey());
      List<ScoreValue> values = new ArrayList<>();
      for (Entry<String, ExecSampleThread> entry : dateMapEntry.getValue().entrySet()) {
        values.add(new ScoreValue().setName(entry.getKey())
            .setCvsValue(entry.getValue().toCsvStringValue()));
      }
      vo.setValues(values);
      vos.add(vo);
    }
    vos.sort(Comparator.comparing(ExecSampleVo::getTimestamp));
    return vos;
  }

  public static List<ExecSampleVo> toErrorListVo(List<ExecSampleError> samples) {
    Map<Long, Map<String, ExecSampleError/*List<ExecSampleSummary>*/>>
        timestampAndNameGroup = samples.stream().collect(
        Collectors.groupingBy(ExecSampleError::getTimestamp,
            Collectors.toMap(ExecSampleError::getName, x -> x)
            /*Collectors.groupingBy(ExecSampleSummary::getName)*/));
    List<ExecSampleVo> vos = new ArrayList<>();
    for (Entry<Long, Map<String, ExecSampleError>> dateMapEntry : timestampAndNameGroup
        .entrySet()) {
      ExecSampleVo vo = new ExecSampleVo();
      vo.setTimestamp0(dateMapEntry.getKey());
      List<ScoreValue> values = new ArrayList<>();
      for (Entry<String, ExecSampleError> entry : dateMapEntry.getValue().entrySet()) {
        values.add(new ScoreValue().setName(entry.getKey())
            .setCvsValue(entry.getValue().toCsvStringValue()));
      }
      vo.setValues(values);
      vos.add(vo);
    }
    vos.sort(Comparator.comparing(ExecSampleVo::getTimestamp));
    return vos;
  }

  public static ExecSampleErrorContentVo toErrorContentListVo(ExecSampleErrorCause sample) {
    return new ExecSampleErrorContentVo()
        .setFinish(sample.isFinish())
        .setName(sample.getName())
        .setNodeId(sample.getNodeId())
        .setTimestamp0(sample.getTimestamp0())
        .setKey(sample.getKey())
        .setContent(sample.getContent());
  }

  public static ExecSampleUploadResultProgressVo toUploadResultProgress(
      ExecSampleUploadResultProgress progress) {
    return new ExecSampleUploadResultProgressVo()
        .setFinish(progress.getFinish())
        .setNodeId(progress.getNodeId())
        .setUploadResultBytes(progress.getUploadResultBytes())
        .setUploadResultTotalBytes(progress.getUploadResultTotalBytes())
        .setUploadResultProgress(progress.getUploadResultProgress());
  }

  @SneakyThrows
  public static LinkedHashMap<String, LinkedHashMap<String, Long>> toCounter(
      List<ExecSampleCounter> counters,
      LinkedHashMap<String, List<String>> pipelineTargetMappings) {
    if (isEmpty(counters)) {
      return null;
    }
    Map<String, List<ExecSampleCounter>> nameMaps = counters.stream()
        .collect(Collectors.groupingBy(ExecSampleCounter::getName));
    LinkedHashMap<String, LinkedHashMap<String, Long>> vos = new LinkedHashMap<>();
    for (Entry<String, List<String>> entry : pipelineTargetMappings.entrySet()) {
      LinkedHashMap<String, Long> nodeCounters = new LinkedHashMap<>();
      List<ExecSampleCounter> nameCounters = nameMaps.get(entry.getKey());
      if (isNotEmpty(nameCounters)) {
        for (ExecSampleCounter counter : nameCounters) {
          if (isNotEmpty(counter.getCounter())) {
            Map<String, Long> nodeCounter = JsonUtils.convert(
                counter.getCounter(), new TypeReference<LinkedHashMap<String, Long>>() {
                });
            if (isNotEmpty(nodeCounter)) {
              for (Entry<String, Long> counter0 : nodeCounter.entrySet()) {
                if (nodeCounters.containsKey(counter0.getKey())) {
                  nodeCounters.put(counter0.getKey(), nodeCounters.get(counter0.getKey())
                      + counter0.getValue());
                } else {
                  nodeCounters.put(counter0.getKey(), counter0.getValue());
                }
              }
            }
          }
        }
      }
      vos.put(entry.getKey(), nodeCounters);
    }
    return vos;
  }

  public static ExecSampleContentInfo toExecSampleExtcVo(ExecSampleContent sample) {
    return new ExecSampleContentInfo()
        .setFinish(sample.isFinish())
        .setName(sample.getName())
        .setNodeId(sample.getNodeId())
        .setTimestamp0(sample.getTimestamp0())
        .setExtField(sample.getExtField())
        .setIteration(sample.getIteration())
        .setKey(sample.getKey())
        .setContent(sample.getContent());
  }

  public static GenericSpecification<ExecSampleSummary> getSummarySpecification(
      ExecSampleFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleScore> getScoreSpecification(ExecSampleFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleThroughput> getThroughputSpecification(
      ExecSampleFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleThread> getThreadSpecification(
      ExecSampleFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleError> getErrorSpecification(ExecSampleFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleErrorCause> getErrorContentSpecification(
      ExecSampleErrorContentFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }

  public static GenericSpecification<ExecSampleContent> getSampleExtcSpecification(
      ExecSampleExtcFindDto dto) {
    return new GenericSpecification<>(new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build());
  }
}
