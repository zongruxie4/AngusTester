package cloud.xcan.sdf.core.angustester.application.cmd.exec;

import cloud.xcan.sdf.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.sdf.api.commonlink.exec.TestResultInfo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ExecTestCmd {

  void testResultUpdate(TestResultInfo scenarioResult, List<TestCaseResultInfo> caseResults);

  IdKey<Long, Object> importExample(Long id);
}
