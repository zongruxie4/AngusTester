package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.MockServiceMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.remote.PageResult;

public interface MockServiceMetricsFacade {

  PageResult<MockServiceMetricsVo> metrics(Long id, NodeMetricsFindDto dto);
}
