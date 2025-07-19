package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistoryInfo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioMonitorHistoryQuery {

  ScenarioMonitorHistory detail(Long id);

  Page<ScenarioMonitorHistoryInfo> list(GenericSpecification<ScenarioMonitorHistoryInfo> spec,
      PageRequest pageable);

  List<ScenarioMonitorHistoryInfo> findInfoById(Collection<Long> monitorIds);

  ScenarioMonitorHistory checkAndFind(Long id);

}
