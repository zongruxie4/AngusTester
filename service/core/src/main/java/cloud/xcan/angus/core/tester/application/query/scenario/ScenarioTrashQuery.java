package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;

public interface ScenarioTrashQuery {

  Long count(Long projectId);

  ScenarioTrash findMyTrashForBiz(Long id, String biz);

}


