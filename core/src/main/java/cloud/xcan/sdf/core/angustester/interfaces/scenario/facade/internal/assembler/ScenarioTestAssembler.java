package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler;


import static cloud.xcan.sdf.core.angustester.application.cmd.task.impl.TaskCmdImpl.getTaskCode;

import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.test.ScenarioTestTaskGenerateDto;
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
        .setOverdueFlag(false)
        .setCode(getTaskCode())
        .setBacklogFlag(false) // Assign sprint is required or is general project management
    ).collect(Collectors.toList());
  }
}




