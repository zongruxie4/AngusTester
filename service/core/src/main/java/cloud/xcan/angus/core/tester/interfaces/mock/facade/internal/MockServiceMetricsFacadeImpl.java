package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceMetricsAssembler.getSpecification;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.query.mock.MockServiceMetricsQuery;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceMetricsFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceMetricsAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.MockServiceMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MockServiceMetricsFacadeImpl implements MockServiceMetricsFacade {

  @Resource
  private MockServiceMetricsQuery mockServiceMetricsQuery;

  @Override
  public PageResult<MockServiceMetricsVo> metrics(Long id, NodeMetricsFindDto dto) {
    Page<JvmServiceUsage> page = mockServiceMetricsQuery.metrics(id,
        getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, MockServiceMetricsAssembler::toMetricsVo);
  }
}
