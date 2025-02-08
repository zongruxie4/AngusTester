package cloud.xcan.sdf.core.angustester.interfaces.services.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.services.testing.TestTaskSetting;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import cloud.xcan.sdf.model.script.ScriptSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ServicesTestAssembler {

  public static List<TestTaskSetting> toTestTaskTestings(Set<ServicesTestTaskGenerateDto> dtos) {
    List<TestTaskSetting> testTaskSettings = new ArrayList<>();
    for (ServicesTestTaskGenerateDto dto : dtos) {
      testTaskSettings.add(new TestTaskSetting()
          .setAssigneeId(dto.getAssigneeId())
          .setTestType(dto.getTestType()).setPriority(dto.getPriority())
          .setDeadlineDate(dto.getDeadlineDate()));
    }
    return testTaskSettings;
  }

  public static List<Script> generateToTaskScript(Set<ServicesTestTaskGenerateDto> dtos) {
    /* Note: Generating functional test scripts is not supported, manual generation of test cases is required*/
    return dtos.stream().filter(x -> !x.getTestType().isFunctional())
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
