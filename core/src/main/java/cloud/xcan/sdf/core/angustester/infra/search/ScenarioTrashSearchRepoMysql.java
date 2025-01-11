package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrashSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ScenarioTrashSearchRepoMysql extends SimpleSearchRepository<ScenarioTrash>
    implements ScenarioTrashSearchRepo {

}
