package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ScenarioMonitorSearchMysql extends SimpleSearchRepository<ScenarioMonitor>
    implements ScenarioMonitorSearchRepo {

}
