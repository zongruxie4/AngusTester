package cloud.xcan.angus.core.tester.application.query.config;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;

public interface NodeNetworkUsageQuery {

  Map<String, NetUsage> networkInfo(Long nodeId);

  List<NetDeviceUsage> network(Long nodeId, GenericSpecification<NetUsage> spec,
      PageRequest pageable);
}
