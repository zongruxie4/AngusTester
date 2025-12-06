package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.spec.experimental.IdKey;
import org.springframework.web.multipart.MultipartFile;

public interface TemplateCmd {

  IdKey<Long, Object> add(Template template);

  void update(Template template);

  void delete(Long id);

  IdKey<Long, Object> imports(TemplateType templateType, String name, MultipartFile file);

}

