package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioFollowQuery {

  Page<ScenarioFollow> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




