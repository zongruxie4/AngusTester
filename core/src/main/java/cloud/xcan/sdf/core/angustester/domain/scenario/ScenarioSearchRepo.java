package cloud.xcan.sdf.core.angustester.domain.scenario;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioSearchRepo extends CustomBaseRepository<Scenario> {

}
