package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.TestTemplateAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.TestTemplateAssembler.updateDtoToDomain;

import cloud.xcan.angus.core.tester.application.cmd.test.TestTemplateCmd;
import cloud.xcan.angus.core.tester.application.query.test.TestTemplateQuery;
import cloud.xcan.angus.core.tester.interfaces.test.facade.TestTemplateFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.TestTemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.TestTemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.TestTemplateAssembler;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.TestTemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TestTemplateFacadeImpl implements TestTemplateFacade {

  @Resource
  private TestTemplateCmd testTemplateCmd;

  @Resource
  private TestTemplateQuery testTemplateQuery;

  @Override
  public IdKey<Long, Object> add(TestTemplateAddDto dto) {
    return testTemplateCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(TestTemplateUpdateDto dto) {
    testTemplateCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    testTemplateCmd.delete(id);
  }

  @Override
  public List<TestTemplateListVo> list() {
    return testTemplateQuery.findAll().stream().map(TestTemplateAssembler::toListVo).toList();
  }
}

