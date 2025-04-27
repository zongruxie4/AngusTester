package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;


import cloud.xcan.angus.api.tester.exec.dto.TestResultUpdateDto;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecTestFacade;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExecTestFacadeImpl implements ExecTestFacade {

  @Resource
  private ExecTestCmd execTestCmd;

  @Override
  public void testResultUpdate(TestResultUpdateDto dto) {
    execTestCmd.testResultUpdate(dto.getTestResult(), dto.getCaseResults());
  }

}
