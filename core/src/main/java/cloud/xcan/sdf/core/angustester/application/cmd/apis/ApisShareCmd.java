package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import java.util.Collection;

public interface ApisShareCmd {

  ApisShare add(ApisShare share);

  void update(ApisShare share);

  void delete(Collection<Long> ids);

  void incrView(Long id);
}
