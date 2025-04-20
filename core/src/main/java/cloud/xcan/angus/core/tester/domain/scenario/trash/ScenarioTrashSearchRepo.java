package cloud.xcan.angus.core.tester.domain.scenario.trash;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScenarioTrashSearchRepo extends CustomBaseRepository<ScenarioTrash> {


}
