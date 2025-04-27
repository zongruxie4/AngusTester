package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;


import static cloud.xcan.angus.core.tester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;

import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ScenarioTestAssembler {

  public static List<Task> generateToTask(Long scenarioId,
      Set<ScenarioTestTaskGenerateDto> testings) {
    return testings.stream().map(testing -> new Task()
        .setTargetId(scenarioId)
        .setTaskType(TaskType.SCENARIO_TEST)
        .setTestType(testing.getTestType())
        .setPriority(testing.getPriority())
        .setAssigneeId(testing.getAssigneeId())
        .setDeadlineDate(testing.getDeadlineDate())
        .setOverdue(false)
        .setCode(getTaskCode())
        .setBacklog(false) // Assign sprint is required or is general project management
    ).collect(Collectors.toList());
  }
}




