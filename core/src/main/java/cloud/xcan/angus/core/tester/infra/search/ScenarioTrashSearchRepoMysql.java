package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrashSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ScenarioTrashSearchRepoMysql extends SimpleSearchRepository<ScenarioTrash>
    implements ScenarioTrashSearchRepo {

}
