package cloud.xcan.angus.core.tester.application.cmd.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.angus.spec.experimental.IdKey;

/**
 * @author XiaoLong Liu
 */public interface MockApisLogCmd {

  IdKey<Long, Object> add0(MockApisLog log);

}
