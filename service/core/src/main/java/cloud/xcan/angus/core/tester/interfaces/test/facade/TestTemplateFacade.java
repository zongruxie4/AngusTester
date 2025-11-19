package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.TestTemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.TestTemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.TestTemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface TestTemplateFacade {

  IdKey<Long, Object> add(TestTemplateAddDto dto);

  void update(TestTemplateUpdateDto dto);

  void delete(Long id);

  List<TestTemplateListVo> list();

}

