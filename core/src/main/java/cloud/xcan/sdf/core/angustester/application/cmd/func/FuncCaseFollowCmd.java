package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.follow.FuncCaseFollow;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface FuncCaseFollowCmd {

  IdKey<Long, Object> add(FuncCaseFollow follow);

  void cancel(Long caseId);

  void cancelAll(Long projectId);
}




