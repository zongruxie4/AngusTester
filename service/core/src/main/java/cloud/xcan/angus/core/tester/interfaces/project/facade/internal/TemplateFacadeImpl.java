package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.tester.application.converter.TemplateConverter.toTemplateContentExportResource;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.TemplateAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.project.facade.internal.TemplateAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.ServletUtils.buildDownloadResourceResponseEntity;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import cloud.xcan.angus.core.tester.application.cmd.project.TemplateCmd;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.core.tester.interfaces.project.facade.TemplateFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateImportDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
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

  @Override
  public IdKey<Long, Object> imports(TemplateImportDto dto) {
    return templateCmd.imports(dto.getTemplateType(), dto.getName(), dto.getFile());
  }

  @Override
  public ResponseEntity<org.springframework.core.io.Resource> export(Long id, String format,
      HttpServletResponse response) {
    // Query single template by ID
    Template template = templateQuery.checkAndFind(id);
    TemplateContent templateContent = template.getTemplateContent();

    if (templateContent == null) {
      throw new IllegalArgumentException("模版内容为空，无法导出");
    }

    // Generate file name based on template name
    String templateName = template.getName() != null ? template.getName()
        .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_") : "Template";
    String fileName = templateName + "-" + System.currentTimeMillis();
    String fileExtension = format.toLowerCase();

    switch (fileExtension) {
      case "excel":
      case "xlsx":
        fileName += ".xlsx";
        break;
      case "csv":
        fileName += ".csv";
        break;
      case "json":
        fileName += ".json";
        break;
      default:
        fileName += ".xlsx"; // Default to Excel
        fileExtension = "xlsx";
    }

    // Export template content only
    org.springframework.core.io.InputStreamResource resource = toTemplateContentExportResource(
        templateContent, template.getTemplateType(), fileName, fileExtension);
    return buildDownloadResourceResponseEntity(-1, APPLICATION_OCTET_STREAM,
        fileName, 0, resource);
  }
}

