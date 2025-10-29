package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;


import static cloud.xcan.angus.core.tester.application.cmd.issue.impl.TaskCmdImpl.getTaskCode;

import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
import java.util.List;
import java.util.Set;

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
    ).toList();
  }
}




