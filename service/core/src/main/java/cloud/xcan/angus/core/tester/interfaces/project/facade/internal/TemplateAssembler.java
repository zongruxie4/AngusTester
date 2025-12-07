package cloud.xcan.angus.core.tester.interfaces.project.facade.internal;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.EXPORT_TEMPLATE_LIST;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.arrayToLists;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotEmpty;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.CaseTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.IssueTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.PlanTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.template.TemplateUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.template.TemplateListVo;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
        .setLastModifiedBy(template.getLastModifiedBy())
        .setLastModifiedDate(template.getLastModifiedDate());
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
  private static InputStreamResource toExcelExportResource(List<TemplateListVo> templates, String fileName) {
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
  private static InputStreamResource toCsvExportResource(List<TemplateListVo> templates, String fileName) {
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
            escapeCsv(template.getCreatedByName()),
            template.getCreatedDate() != null ? template.getCreatedDate().toString() : "",
            escapeCsv(template.getLastModifiedByName()),
            template.getLastModifiedDate() != null ? template.getLastModifiedDate().toString() : ""));
      }
    }
    
    return new InputStreamResource(new FileInputStream(filePath));
  }

  /**
   * Converts template list to JSON export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toJsonExportResource(List<TemplateListVo> templates, String fileName) {
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

  /**
   * Converts template content to export resource (Excel, CSV, or JSON format).
   * Only exports template content, not template metadata.
   */
  @SneakyThrows
  @NotNull
  public static InputStreamResource toTemplateContentExportResource(TemplateContent templateContent,
      TemplateType templateType, String fileName, String format) {
    String fileExtension = format.toLowerCase();
    
    if ("json".equals(fileExtension)) {
      return toTemplateContentJsonExportResource(templateContent);
    } else if ("csv".equals(fileExtension)) {
      return toTemplateContentCsvExportResource(templateContent, templateType, fileName);
    } else {
      // Default to Excel
      return toTemplateContentExcelExportResource(templateContent, templateType, fileName);
    }
  }

  /**
   * Converts template content to Excel export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toTemplateContentExcelExportResource(
      TemplateContent templateContent, TemplateType templateType, String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    
    // Get headers and data based on template type
    List<String> headers = getTemplateContentHeaders(templateType);
    List<String> data = getTemplateContentData(templateContent, templateType);
    
    // Convert headers to List<List<String>> format for EasyExcel
    List<List<String>> headerList = headers.stream()
        .map(List::of)
        .collect(Collectors.toList());
    
    // Convert data to List<List<String>> format for EasyExcel
    List<List<String>> dataList = List.of(data);
    
    EasyExcel.write(file)
        .head(headerList).sheet()
        .registerWriteHandler(new SimpleColumnWidthStyleStrategy(25))
        .doWrite(dataList);
    
    return new InputStreamResource(new FileInputStream(filePath));
  }

  /**
   * Converts template content to CSV export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toTemplateContentCsvExportResource(
      TemplateContent templateContent, TemplateType templateType, String fileName) {
    String filePath = new SpringAppDirUtils().getTmpDir() + TesterConstant.EXPORT_SUMMARY_DIR
        + getTenantId() + File.separator + fileName;
    File file = new File(filePath);
    FileUtils.forceMkdirParent(file);
    
    // Get headers and data based on template type
    List<String> headers = getTemplateContentHeaders(templateType);
    List<String> data = getTemplateContentData(templateContent, templateType);
    
    try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
      // Write header
      writer.write(String.join(",", headers.stream().map(TemplateAssembler::escapeCsv).toList()) + "\n");
      
      // Write data
      writer.write(String.join(",", data.stream().map(TemplateAssembler::escapeCsv).toList()) + "\n");
    }
    
    return new InputStreamResource(new FileInputStream(filePath));
  }

  /**
   * Converts template content to JSON export resource.
   */
  @SneakyThrows
  @NotNull
  private static InputStreamResource toTemplateContentJsonExportResource(TemplateContent templateContent) {
    String jsonContent = JsonUtils.toJson(templateContent);
    byte[] contentBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
    return new InputStreamResource(new ByteArrayInputStream(contentBytes));
  }

  /**
   * Gets headers for template content export based on template type.
   */
  private static List<String> getTemplateContentHeaders(TemplateType templateType) {
    switch (templateType) {
      case ISSUE:
        return List.of("任务类型", "缺陷等级", "优先级", "是否漏测缺陷", "评估工作量方法", "评估工作量", "实际工作量", "描述");
      case TEST_PLAN:
        return List.of("测试范围", "测试目标", "验收标准", "其他信息");
      case TEST_CASE:
        return List.of("测试层级", "测试目的", "前置条件", "步骤视图", "测试步骤", "描述");
      default:
        return List.of();
    }
  }

  /**
   * Gets data row for template content export based on template type.
   */
  private static List<String> getTemplateContentData(TemplateContent templateContent, TemplateType templateType) {
    List<String> data = new ArrayList<>();
    
    switch (templateType) {
      case ISSUE:
        if (templateContent instanceof IssueTemplateContent issueContent) {
          data.add(issueContent.getTaskType() != null ? issueContent.getTaskType().name() : "");
          data.add(issueContent.getBugLevel() != null ? issueContent.getBugLevel().name() : "");
          data.add(issueContent.getPriority() != null ? issueContent.getPriority().name() : "");
          data.add(issueContent.getMissingBug() != null && issueContent.getMissingBug() ? "是" : "否");
          data.add(issueContent.getEvalWorkloadMethod() != null ? issueContent.getEvalWorkloadMethod().name() : "");
          data.add(issueContent.getEvalWorkload() != null ? issueContent.getEvalWorkload().toString() : "");
          data.add(issueContent.getActualWorkload() != null ? issueContent.getActualWorkload().toString() : "");
          data.add(issueContent.getDescription() != null ? issueContent.getDescription() : "");
        }
        break;
      case TEST_PLAN:
        if (templateContent instanceof PlanTemplateContent planContent) {
          data.add(planContent.getTestingScope() != null ? planContent.getTestingScope() : "");
          data.add(planContent.getTestingObjectives() != null ? planContent.getTestingObjectives() : "");
          data.add(planContent.getAcceptanceCriteria() != null ? planContent.getAcceptanceCriteria() : "");
          data.add(planContent.getOtherInformation() != null ? planContent.getOtherInformation() : "");
        }
        break;
      case TEST_CASE:
        if (templateContent instanceof CaseTemplateContent caseContent) {
          data.add(caseContent.getTestLayer() != null ? caseContent.getTestLayer().name() : "");
          data.add(caseContent.getTestPurpose() != null ? caseContent.getTestPurpose().name() : "");
          data.add(caseContent.getPrecondition() != null ? caseContent.getPrecondition() : "");
          data.add(caseContent.getStepView() != null ? caseContent.getStepView().name() : "");
          // Format steps: step1##expected1@@step2##expected2
          String stepsStr = "";
          if (caseContent.getSteps() != null && !caseContent.getSteps().isEmpty()) {
            stepsStr = caseContent.getSteps().stream()
                .map(step -> step.getStep() + "##" + (step.getExpectedResult() != null ? step.getExpectedResult() : ""))
                .collect(java.util.stream.Collectors.joining("@@"));
          }
          data.add(stepsStr);
          data.add(caseContent.getDescription() != null ? caseContent.getDescription() : "");
        }
        break;
    }
    
    return data;
  }
}

