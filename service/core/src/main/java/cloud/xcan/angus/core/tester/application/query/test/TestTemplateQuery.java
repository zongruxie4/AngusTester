package cloud.xcan.angus.core.tester.application.query.test;

import cloud.xcan.angus.core.tester.domain.test.template.TestTemplate;
import java.util.List;

public interface TestTemplateQuery {

  TestTemplate checkAndFind(Long id);

  List<TestTemplate> findAll();

}

