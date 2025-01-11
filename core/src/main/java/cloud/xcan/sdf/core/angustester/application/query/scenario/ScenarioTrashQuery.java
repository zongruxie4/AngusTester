package cloud.xcan.sdf.core.angustester.application.query.scenario;

import cloud.xcan.sdf.core.angustester.domain.scenario.trash.ScenarioTrash;

public interface ScenarioTrashQuery {

  Long count(Long projectId);

  ScenarioTrash findMyTrashForBiz(Long id, String biz);

}


