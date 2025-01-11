package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioMonitorHistoryQuery {

  ScenarioMonitorHistory detail(Long id);

  Page<ScenarioMonitorHistoryInfo> find(GenericSpecification<ScenarioMonitorHistoryInfo> spec,
      PageRequest pageable);

  List<ScenarioMonitorHistoryInfo> findInfoById(Collection<Long> monitorIds);

  ScenarioMonitorHistory checkAndFind(Long id);

}
