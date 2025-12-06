package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface TemplateFacade {

  IdKey<Long, Object> add(TemplateAddDto dto);

  void update(TemplateUpdateDto dto);

  void delete(Long id);

  List<TemplateListVo> list();

}

