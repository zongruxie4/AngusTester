package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;

public class TemplateAssembler {

  public static Template addDtoToDomain(TemplateAddDto dto) {
    return new Template()
        .setName(dto.getName())
        .setTemplateType(dto.getTemplateType())
        .setTemplateContent(dto.getTemplateContent())
        .setIsSystem(false);
  }

  public static Template updateDtoToDomain(TemplateUpdateDto dto) {
    return new Template()
        .setId(dto.getId())
        .setName(dto.getName())
        .setTemplateType(dto.getTemplateType())
        .setTemplateContent(dto.getTemplateContent());
  }

  public static TemplateListVo toListVo(Template template) {
    return new TemplateListVo()
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

