package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TemplateCmd {

  IdKey<Long, Object> add(Template template);

  void update(Template template);

  void delete(Long id);

}

