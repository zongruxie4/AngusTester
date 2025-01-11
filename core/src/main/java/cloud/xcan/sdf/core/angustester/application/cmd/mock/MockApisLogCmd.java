package cloud.xcan.sdf.core.angustester.application.cmd.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.spec.experimental.IdKey;

/**
 * @author xiaolong.liu
 */
public interface MockApisLogCmd {

  IdKey<Long, Object> add0(MockApisLog log);

}
