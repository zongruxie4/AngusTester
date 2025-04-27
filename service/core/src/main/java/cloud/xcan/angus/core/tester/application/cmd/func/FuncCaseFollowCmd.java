package cloud.xcan.angus.core.tester.application.cmd.func;

import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollow;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFollowCmd {

  IdKey<Long, Object> add(FuncCaseFollow follow);

  void cancel(Long caseId);

  void cancelAll(Long projectId);
}




