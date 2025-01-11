package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollow;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface ApisFollowCmd {

  IdKey<Long, Object> add(ApisFollow apisFollow);

  void cancel(Long apisId);

  void cancelAll(Long projectId);
}




