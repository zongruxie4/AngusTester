package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler;

import static cloud.xcan.sdf.core.angustester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;
import static cloud.xcan.sdf.core.angustester.application.converter.ApisToAngusModelConverter.getScriptTaskArguments;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.Threads;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestTaskGenerateDto;
import cloud.xcan.sdf.model.script.ScriptSource;
import cloud.xcan.sdf.model.script.TestType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ApisTestAssembler {

  public static List<Script> generateToScript(Set<ApisTestScriptGenerateDto> dtos) {
    List<Script> scripts = new ArrayList<>();
    Map<TestType, ApisTestScriptGenerateDto> typeMap = dtos.stream()
        .collect(Collectors.toMap(ApisTestScriptGenerateDto::getTestType, x -> x));
    if (typeMap.containsKey(TestType.FUNCTIONAL)) {
      ApisTestScriptGenerateDto testing = typeMap.get(TestType.FUNCTIONAL);
      Arguments arguments = getScriptTaskArguments(testing.getIgnoreAssertions());
      Script script = new Script()
          .setType(ScriptType.TEST_FUNCTIONALITY)
          .setSource(ScriptSource.API)
          //.setSourceId(apisId)
          .setAuthFlag(nullSafe(testing.getAuthFlag(), false))
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_FUNCTIONALITY)
              .configuration(Configuration.newBuilder()
                  .iterations(nullSafe(testing.getIterations(), 1L))
                  .thread(Threads.newBuilder()
                      .threads(1)
                      .build())
                  .priority(testing.getPriority().toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }

    if (typeMap.containsKey(TestType.PERFORMANCE)) {
      ApisTestScriptGenerateDto testing = typeMap.get(TestType.PERFORMANCE);
      Arguments arguments = getScriptTaskArguments(testing.getIgnoreAssertions());
      Script script = new Script()
          .setType(ScriptType.TEST_PERFORMANCE)
          .setSource(ScriptSource.API)
          //.setSourceId(apisId)
          .setAuthFlag(nullSafe(testing.getAuthFlag(), false))
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_PERFORMANCE)
              .configuration(Configuration.newBuilder()
                  .duration(testing.getDuration())
                  .thread(Threads.newBuilder()
                      .threads(testing.getThreads())
                      .rampUpThreads(testing.getRampUpThreads())
                      .rampUpInterval(testing.getRampUpInterval())
                      .build())
                  .priority(testing.getPriority().toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }

    if (typeMap.containsKey(TestType.STABILITY)) {
      ApisTestScriptGenerateDto testing = typeMap.get(TestType.STABILITY);
      Arguments arguments = getScriptTaskArguments(testing.getIgnoreAssertions());
      Script script = new Script()
          .setType(ScriptType.TEST_STABILITY)
          .setSource(ScriptSource.API)
          //.setSourceId(apisId)
          .setAuthFlag(nullSafe(testing.getAuthFlag(), false))
          .setAngusScript(AngusScript.newBuilder()
              .type(ScriptType.TEST_STABILITY)
              .configuration(Configuration.newBuilder()
                  .duration(testing.getDuration())
                  .thread(Threads.newBuilder()
                      .threads(testing.getThreads())
                      //.rampUpThreads(testing.getRampUpThreads())
                      //.rampUpInterval(testing.getRampUpInterval())
                      .build())
                  .priority(testing.getPriority().toExecPriority())
                  .build())
              .task(cloud.xcan.angus.model.script.pipeline.Task.newBuilder()
                  .arguments(arguments)
                  .build())
              .build());
      scripts.add(script);
    }
    return scripts;
  }

  public static List<Task> generateToTask(Long apisId, Set<ApisTestTaskGenerateDto> dtos) {
    return dtos.stream().map(testing -> new Task()
        .setTargetId(apisId)
        .setTaskType(TaskType.API_TEST)
        .setTestType(testing.getTestType())
        .setPriority(testing.getPriority())
        .setAssigneeId(testing.getAssigneeId())
        .setDeadlineDate(testing.getDeadlineDate())
        .setOverdueFlag(false)
        .setCode(getTaskCode())
        .setBacklogFlag(false) // Assign sprint is required or is general project management
    ).collect(Collectors.toList());
  }

}
