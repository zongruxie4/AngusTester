package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import org.springframework.stereotype.Repository;

@Repository("scenarioRepo")
public interface ScenarioRepoPostgres extends ScenarioRepo {


}
