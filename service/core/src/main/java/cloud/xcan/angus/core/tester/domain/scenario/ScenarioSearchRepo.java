package cloud.xcan.angus.core.tester.domain.scenario;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioSearchRepo extends CustomBaseRepository<Scenario> {

}
