package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo;

import cloud.xcan.angus.agent.message.runner.RunnerLogVo;
import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecNodeLogVo {

  private Long nodeId;
  private String nodeName;

  private RunnerRunVo schedulingResult;

  private RunnerLogVo runnerLog;

}
