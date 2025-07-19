package cloud.xcan.angus.core.tester.application.query.scenario;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.scenario.trash.ScenarioTrash;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ScenarioTrashQuery {

  Long count(Long projectId);

  Page<ScenarioTrash> list(GenericSpecification<ScenarioTrash> spec,
      PageRequest pageable, boolean fullTextSearch, String[] match);

  ScenarioTrash findMyTrashForBiz(Long id, String biz);

}


