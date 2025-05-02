package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.agent.message.mockservice.StatusVo;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.MockServiceStatusDto;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MockServiceMetricsQuery {

  Page<JvmServiceUsage> metrics(Long id, GenericSpecification<JvmServiceUsage> spec,
      PageRequest pageable);

  List<StatusVo> status(MockServiceStatusDto dto);

  Set<Long> getLiveServiceIds(Collection<Long> serviceIds);

}
