package cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.services.testing.TestTaskSetting;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import cloud.xcan.angus.model.script.ScriptSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServicesTestAssembler {

  public static List<TestTaskSetting> toTestTaskTestings(Set<ServicesTestTaskGenerateDto> dto) {
    List<TestTaskSetting> testTaskSettings = new ArrayList<>();
    for (ServicesTestTaskGenerateDto dto : dto) {
      testTaskSettings.add(new TestTaskSetting()
          .setAssigneeId(dto.getAssigneeId())
          .setTestType(dto.getTestType()).setPriority(dto.getPriority())
          .setDeadlineDate(dto.getDeadlineDate()));
    }
    return testTaskSettings;
  }

  public static List<Script> generateToTaskScript(Set<ServicesTestTaskGenerateDto> dto) {
    /* Note: Generating functional test scripts is not supported, manual generation of test cases is required*/
    return dto.stream().filter(x -> !x.getTestType().isFunctional())
        .map(testing -> new Script()
                .setType(testing.getTestType().toScriptType())
                .setSource(ScriptSource.API)
            /*.setAngusScript(AngusScript.newBuilder()
                .type(testing.getTestType().toScriptType())
                .configuration(Configuration.newBuilder()
                    .duration(testing.getDuration())
                    .thread(Threads.newBuilder()
                        .threads(testing.getThreads())
                        .build())
                    .priority(testing.getPriority().toExecPriority())
                    .build())
                .build())*/
        ).collect(Collectors.toList());
  }
}
