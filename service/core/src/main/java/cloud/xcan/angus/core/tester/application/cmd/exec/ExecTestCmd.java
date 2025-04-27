package cloud.xcan.angus.core.tester.application.cmd.exec;

import cloud.xcan.angus.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.angus.api.commonlink.exec.TestResultInfo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface ExecTestCmd {

  void testResultUpdate(TestResultInfo scenarioResult, List<TestCaseResultInfo> caseResults);

  IdKey<Long, Object> importExample(Long projectId);

}
