package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ScenarioMonitorSearchMysql extends SimpleSearchRepository<ScenarioMonitor>
    implements ScenarioMonitorSearchRepo {

}
