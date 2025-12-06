package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.core.tester.domain.project.template.Template;
import java.util.List;

public interface TemplateQuery {

  Template checkAndFind(Long id);

  List<Template> findAll();

}

