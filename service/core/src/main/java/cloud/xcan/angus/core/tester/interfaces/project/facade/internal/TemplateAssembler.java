package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_TEMPLATE_LIST;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotEmpty;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.arrayToLists;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;

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
        .setModifiedBy(template.getModifiedBy())
        .setModifiedDate(template.getModifiedDate());
  }

  /**
   * Converts template list to export resource (Excel, CSV, or JSON format).
   */
  @SneakyThrows
  @NotNull
  public static InputStreamResource toTemplateListExportResource(List<TemplateListVo> templates,
      String fileName, String format) {
    String fileExtension = format.toLowerCase();

    if ("json".equals(fileExtension)) {
      return toJsonExportResource(templates, fileName);
    } else if ("csv".equals(fileExtension)) {
      return toCsvExportResource(templates, fileName);
    } else {
      // Default to Excel
      return toExcelExportResource(templates, fileName);
    }
  }

  /**
   * Converts template list to Excel export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toExcelExportResource(List<TemplateListVo> templates,
      String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);

    String headerMessage = message(EXPORT_TEMPLATE_LIST);
    assertNotEmpty(headerMessage, "TemplateListExport message not configured");

    EasyExcel.write(file, TemplateListVo.class)
        .head(arrayToLists(headerMessage.split(","))).sheet()
        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
        .doWrite(() -> templates);

    return new InputStreamResource(new FileInputStream(filePath));
  }

  /**
   * Converts template list to CSV export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toCsvExportResource(List<TemplateListVo> templates,
      String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);

    try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
      // Write header
      writer.write("模版名称,模版类型,是否系统模版,创建人,创建时间,最后修改人,最后修改时间\n");

      // Write data
      for (TemplateListVo template : templates) {
        writer.write(String.format("%s,%s,%s,%s,%s,%s,%s\n",
            escapeCsv(template.getName()),
            template.getTemplateType() != null ? template.getTemplateType().name() : "",
            template.getIsSystem() != null && template.getIsSystem() ? "是" : "否",
            escapeCsv(template.getCreator()),
            template.getCreatedDate() != null ? template.getCreatedDate().toString() : "",
            escapeCsv(template.getModifier()),
            template.getModifiedDate() != null ? template.getModifiedDate().toString()
                : ""));
      }
    }

    return new InputStreamResource(new FileInputStream(filePath));
  }

  /**
   * Converts template list to JSON export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toJsonExportResource(List<TemplateListVo> templates,
      String fileName) {
    String jsonContent = JsonUtils.toJson(templates);
    byte[] contentBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
    return new InputStreamResource(new ByteArrayInputStream(contentBytes));
  }

  /**
   * Escapes CSV special characters.
   */
  private static String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

}

