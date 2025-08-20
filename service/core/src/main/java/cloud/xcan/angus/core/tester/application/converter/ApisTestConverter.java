package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.converter.DefaultModelHttpConverter.HTTP_CONVERTER;
import static cloud.xcan.angus.core.converter.oas3.SchemaToHttpConverter.toServer;
import static cloud.xcan.angus.core.tester.application.converter.ApisToAngusModelConverter.getScriptTaskArguments;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.kanban.TestApisCount;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.model.apis.ApisInfo;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.Threads;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.services.ApisTestCount;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ApisTestConverter {

  public static List<Script> startToScript(Apis apisDb, Set<TestType> testTypes,
      IndicatorPerf indicatorPerf, IndicatorStability indicatorStability) {
    List<Script> scripts = new ArrayList<>();
    Arguments arguments = getScriptTaskArguments(false);
    if (testTypes.contains(TestType.FUNCTIONAL)) {
      Script script = new Script()
          .setType(ScriptType.TEST_FUNCTIONALITY)
          .setSource(ScriptSource.API)
          .setAuth(apisDb.getAuth())
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_FUNCTIONALITY)
              .configuration(Configuration.newBuilder()
                  .iterations(1L)
                  .thread(Threads.newBuilder()
                      .threads(1)
                      .build())
                  .priority(Priority.HIGH.toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }

    if (testTypes.contains(TestType.PERFORMANCE) && nonNull(indicatorPerf)) {
      Script script = new Script()
          .setType(ScriptType.TEST_PERFORMANCE)
          .setSource(ScriptSource.API)
          .setAuth(apisDb.getAuth())
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_PERFORMANCE)
              .configuration(Configuration.newBuilder()
                  .duration(indicatorPerf.getDuration())
                  .thread(Threads.newBuilder()
                      .threads(indicatorPerf.getThreads())
                      .rampUpThreads(indicatorPerf.getRampUpThreads())
                      .rampUpInterval(indicatorPerf.getRampUpInterval())
                      .build())
                  .priority(Priority.DEFAULT.toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }

    if (testTypes.contains(TestType.STABILITY) && nonNull(indicatorStability)) {
      Script script = new Script()
          .setType(ScriptType.TEST_STABILITY)
          .setSource(ScriptSource.API)
          .setAuth(apisDb.getAuth())
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_STABILITY)
              .configuration(Configuration.newBuilder()
                  .duration(indicatorStability.getDuration())
                  .thread(Threads.newBuilder()
                      .threads(indicatorStability.getThreads())
                      //.rampUpThreads(testing.getRampUpThreads())
                      //.rampUpInterval(testing.getRampUpInterval())
                      .build())
                  .priority(Priority.DEFAULT.toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }
    return scripts;
  }

  public static void assembleApisTestCount(ApisTestCount count, List<ApisBaseInfo> apis) {
    List<Long> enabledFuncApisId = apis.stream()
        .filter(x -> nonNull(x.getTestFunc()) && x.getTestFunc())
        .map(ApisBaseInfo::getId).toList();
    count.setEnabledFuncTestNum(enabledFuncApisId.size());
    count.getEnabledTestApiIds().put(TestType.FUNCTIONAL, enabledFuncApisId);

    List<Long> enabledPerfApisId = apis.stream()
        .filter(x -> nonNull(x.getTestPerf()) && x.getTestPerf())
        .map(ApisBaseInfo::getId).toList();
    count.setEnabledPerfTestNum(enabledPerfApisId.size());
    count.getEnabledTestApiIds().put(TestType.PERFORMANCE, enabledPerfApisId);

    List<Long> enabledStabilityApisId = apis.stream()
        .filter(x -> nonNull(x.getTestStability()) && x.getTestStability())
        .map(ApisBaseInfo::getId).toList();
    count.setEnabledStabilityTestNum(enabledStabilityApisId.size());
    count.getEnabledTestApiIds().put(TestType.STABILITY, enabledStabilityApisId);

    count.setTotalApisNum(apis.size());
    count.setEnabledTestNum(count.getEnabledFuncTestNum() + count.getEnabledPerfTestNum()
        + count.getEnabledStabilityTestNum());
    count.setAllApis(apis.stream().map(ApisTestConverter::toApisInfo)
        .toList());
  }

  public static void assembleTestApisCount(TestApisCount count, List<ApisBaseInfo> apis) {
    count.setTotalApiNum(apis.size());
    count.setEnabledTestApiNum(apis.stream().filter(ApisBaseInfo::isEnabledTest).count());
    count.setEnabledTestApiRate(count.calcEnabledTestApiRate());
    count.setPassedTestApiNum(apis.stream().filter(ApisBaseInfo::isPassedTest).count());
    count.setTestApiProgress(count.calcTestApiProgress());

    count.setEnabledTestNum(apis.stream()
        .map(ApisBaseInfo::getEnabledTestNum).reduce(0, Integer::sum));
    count.setPassedTestNum(apis.stream()
        .map(ApisBaseInfo::getPassedTestNum).reduce(0, Integer::sum));
    count.setTestProgress(count.calcTestProgress());
  }

  public static ApisInfo toApisInfo(ApisBaseInfo x) {
    try {
      return new ApisInfo(x.getId(), x.getSummary(), x.getMethod().getValue(),
          HTTP_CONVERTER.toUrl(toServer(x.getCurrentServer()), x.getEndpoint(), null),
          x.getOperationId(), x.getStatus(), x.getTestFunc(), x.getTestPerf(),
          x.getTestStability());
    } catch (JsonProcessingException e) {
      return new ApisInfo(x.getId(), x.getSummary(), x.getMethod().getValue(), x.getEndpoint(),
          x.getOperationId(), x.getStatus(), x.getTestFunc(), x.getTestPerf(),
          x.getTestStability());
    }
  }

}
