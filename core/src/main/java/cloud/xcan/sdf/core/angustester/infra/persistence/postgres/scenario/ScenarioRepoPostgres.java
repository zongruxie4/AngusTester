package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import org.springframework.stereotype.Repository;

@Repository("scenarioRepo")
public interface ScenarioRepoPostgres extends ScenarioRepo {


}
