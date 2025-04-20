package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.api.pojo.ApisRequestLog;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface MockApisLogOpen2pFacade {

  List<IdKey<Long, Object>> add(List<ApisRequestLog> dto);

}
