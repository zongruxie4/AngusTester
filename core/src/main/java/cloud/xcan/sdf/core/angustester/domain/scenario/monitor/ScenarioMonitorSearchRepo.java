package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioMonitorSearchRepo extends CustomBaseRepository<ScenarioMonitor> {

}
