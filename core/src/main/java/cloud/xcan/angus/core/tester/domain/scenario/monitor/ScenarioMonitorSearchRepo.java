package cloud.xcan.angus.core.tester.domain.scenario.monitor;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioMonitorSearchRepo extends CustomBaseRepository<ScenarioMonitor> {

}
