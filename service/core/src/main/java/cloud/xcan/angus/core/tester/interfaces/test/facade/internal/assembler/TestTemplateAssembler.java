package cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.test.template.TestTemplate;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.template.TestTemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.template.TestTemplateListVo;

public class TestTemplateAssembler {

  public static TestTemplate addDtoToDomain(TestTemplateAddDto dto) {
    return new TestTemplate()
        .setName(dto.getName())
        .setTemplateType(dto.getTemplateType())
        .setTemplateContent(dto.getTemplateContent())
        .setIsSystem(false);
  }

  public static TestTemplate updateDtoToDomain(TestTemplateUpdateDto dto) {
    return new TestTemplate()
        .setId(dto.getId())
        .setName(dto.getName())
        .setTemplateType(dto.getTemplateType())
        .setTemplateContent(dto.getTemplateContent());
  }

  public static TestTemplateListVo toListVo(TestTemplate template) {
    return new TestTemplateListVo()
        .setId(template.getId())
        .setName(template.getName())
        .setTemplateType(template.getTemplateType())
        .setTemplateContent(template.getTemplateContent())
        .setIsSystem(template.getIsSystem())
        .setCreatedBy(template.getCreatedBy())
        .setCreatedDate(template.getCreatedDate())
        .setLastModifiedBy(template.getLastModifiedBy())
        .setLastModifiedDate(template.getLastModifiedDate());
  }
}

