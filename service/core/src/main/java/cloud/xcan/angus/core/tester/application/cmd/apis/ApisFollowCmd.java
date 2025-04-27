package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollow;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface ApisFollowCmd {

  IdKey<Long, Object> add(ApisFollow apisFollow);

  void cancel(Long apisId);

  void cancelAll(Long projectId);
}




