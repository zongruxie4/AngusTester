package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.sdf.api.pojo.ApisRequestLog;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface MockApisLogOpen2pFacade {

  List<IdKey<Long, Object>> add(List<ApisRequestLog> dto);

}
