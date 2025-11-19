package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.template.TestTemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface TestTemplateFacade {

  IdKey<Long, Object> add(TestTemplateAddDto dto);

  void update(TestTemplateUpdateDto dto);

  void delete(Long id);

  List<TestTemplateListVo> list();

}

