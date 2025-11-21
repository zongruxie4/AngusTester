package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.MockServiceMetricsVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.NodeMetricsFindDto;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class MockServiceMetricsAssembler {

  public static MockServiceMetricsVo toMetricsVo(JvmServiceUsage usage) {
    return new MockServiceMetricsVo()
        .setTimestamp0(usage.getTimestamp())
        .setCvsJvm(nonNull(usage.getJvm()) ? usage.getJvm().toString() : "")
        .setCvsProcessor(nonNull(usage.getProcessor())
            ? usage.getProcessor().toString() : "");
  }

  public static GenericSpecification<JvmServiceUsage> getSpecification(NodeMetricsFindDto dto) {
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("timestamp")
        .timestampStringToLong(true)
        .build();
    return new GenericSpecification<JvmServiceUsage>(filters);
  }

}
