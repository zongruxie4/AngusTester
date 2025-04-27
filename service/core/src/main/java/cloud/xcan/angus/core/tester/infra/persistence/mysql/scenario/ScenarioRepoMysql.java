package cloud.xcan.angus.core.tester.infra.persistence.mysql.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import org.springframework.stereotype.Repository;

@Repository("scenarioRepo")
public interface ScenarioRepoMysql extends ScenarioRepo {


}
