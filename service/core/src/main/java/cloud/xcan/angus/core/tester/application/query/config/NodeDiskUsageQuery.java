package cloud.xcan.angus.core.tester.application.query.config;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskDeviceUsage;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;

public interface NodeDiskUsageQuery {

  Map<String, DiskUsage> diskInfo(Long nodeId);

  List<DiskDeviceUsage> disk(Long nodeId, GenericSpecification<DiskUsage> spec,
      PageRequest pageable);
}
