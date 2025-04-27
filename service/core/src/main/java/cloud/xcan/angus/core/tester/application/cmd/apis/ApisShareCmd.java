package cloud.xcan.angus.core.tester.application.cmd.apis;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import java.util.Collection;

public interface ApisShareCmd {

  ApisShare add(ApisShare share);

  void update(ApisShare share);

  void delete(Collection<Long> ids);

  void incrView(Long id);
}
