package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollow;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFollowCmd {

  IdKey<Long, Object> add(FuncCaseFollow follow);

  void cancel(Long caseId);

  void cancelAll(Long projectId);
}




