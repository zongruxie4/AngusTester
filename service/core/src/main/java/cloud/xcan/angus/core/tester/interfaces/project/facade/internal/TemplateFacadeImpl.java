package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.TemplateAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.TemplateAssembler.updateDtoToDomain;

import cloud.xcan.angus.core.tester.application.cmd.project.TemplateCmd;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.interfaces.project.facade.TemplateFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TemplateFacadeImpl implements TemplateFacade {

  @Resource
  private TemplateCmd templateCmd;

  @Resource
  private TemplateQuery templateQuery;

  @Override
  public IdKey<Long, Object> add(TemplateAddDto dto) {
    return templateCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(TemplateUpdateDto dto) {
    templateCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    templateCmd.delete(id);
  }

  @Override
  public List<TemplateListVo> list() {
    return templateQuery.findAll().stream().map(TemplateAssembler::toListVo).toList();
  }
}

