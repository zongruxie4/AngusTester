package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.template.TestTemplate;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TestTemplateCmd {

  IdKey<Long, Object> add(TestTemplate template);

  void update(TestTemplate template);

  void delete(Long id);

}

