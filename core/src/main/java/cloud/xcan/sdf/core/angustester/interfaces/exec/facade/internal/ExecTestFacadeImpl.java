package cloud.xcan.sdf.core.angustester.interfaces.exec.facade.internal;


import cloud.xcan.sdf.api.angustester.exec.dto.TestResultUpdateDto;
import cloud.xcan.sdf.core.angustester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.sdf.core.angustester.interfaces.exec.facade.ExecTestFacade;
import javax.annotation.Resource;
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
