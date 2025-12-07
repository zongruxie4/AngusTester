package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateImportDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;

public interface TemplateFacade {

  IdKey<Long, Object> add(TemplateAddDto dto);

  void update(TemplateUpdateDto dto);

  void delete(Long id);

  List<TemplateListVo> list();

  IdKey<Long, Object> imports(TemplateImportDto dto);

  ResponseEntity<org.springframework.core.io.Resource> export(Long id, String format,
      HttpServletResponse response);

}

