package cloud.xcan.angus.core.tester.application.cmd.exec;

import cloud.xcan.angus.core.tester.domain.exec.Exec;

public interface ExecTestResultCmd {

  void generateResult(Exec execDb);
}
