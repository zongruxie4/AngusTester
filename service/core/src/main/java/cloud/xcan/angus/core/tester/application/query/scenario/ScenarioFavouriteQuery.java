package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.favorite.ScenarioFavourite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioFavouriteQuery {

  Page<ScenarioFavourite> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);
}




