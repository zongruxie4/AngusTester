package cloud.xcan.sdf.core.angustester.domain.scenario.trash;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioTrashSearchRepo extends CustomBaseRepository<ScenarioTrash> {


}
