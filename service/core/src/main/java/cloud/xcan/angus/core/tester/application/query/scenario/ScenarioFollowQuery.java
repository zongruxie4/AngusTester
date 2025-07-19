package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioFollowQuery {

  Page<ScenarioFollow> list(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




