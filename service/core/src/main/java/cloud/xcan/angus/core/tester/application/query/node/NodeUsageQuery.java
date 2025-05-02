package cloud.xcan.angus.core.tester.application.query.node;

import cloud.xcan.angus.api.commonlink.exec.result.NodeUsageSummary;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface NodeUsageQuery {

  Page<NodeUsage> metrics(Long id, GenericSpecification<NodeUsage> spec,
      PageRequest pageable);

  NodeUsage metricsLatest(Long id);

  Map<Long, NodeUsageSummary> getUsageSummaries(Collection<Long> nodeIds, LocalDateTime from,
      LocalDateTime to);

  NodeUsageSummary getUsageSummary(Long nodeId, LocalDateTime from, LocalDateTime to);
}
