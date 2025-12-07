package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.spec.locale.SupportedLanguage.zh_CN;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.CaseTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.IssueTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.PlanTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Converter utility class for template import and export operations.
 * <p>
 * Provides static methods for parsing template content from various file formats (Excel, CSV, JSON)
 * and converting template content to export formats.
 */
public class TemplateConverter {

  /**
   * Parses import file based on file extension (Excel, CSV, or JSON) and template type.
   * Returns TemplateContent based on the template type.
   */
  public static TemplateContent parseImportFile(MultipartFile file, String fileName,
      TemplateType templateType) {
    String lowerFileName = fileName.toLowerCase();
    if (lowerFileName.endsWith(".xlsx") || lowerFileName.endsWith(".xls")) {
      return parseExcelFile(file, templateType);
    } else if (lowerFileName.endsWith(".csv")) {
      return parseCsvFile(file, templateType);
    } else if (lowerFileName.endsWith(".json")) {
      return parseJsonFile(file, templateType);
    } else {
      throw ProtocolException.of(
          "不支持的文件格式，仅支持 Excel (.xlsx, .xls)、CSV (.csv) 或 JSON (.json) 格式");
    }
  }

  /**
   * Parses Excel file and converts to TemplateContent based on template type.
   */
  public static TemplateContent parseExcelFile(MultipartFile file, TemplateType templateType) {
    List<String[]> rows;
    try {
      rows = PoiUtils.readExcel(file.getInputStream());
    } catch (IOException e) {
      throw ProtocolException.of("读取 Excel 文件失败: " + e.getMessage());
    }
    assertNotEmpty(rows, "Excel 文件内容为空");

    // First row is header, second row is data
    if (rows.size() < 2) {
      throw ProtocolException.of("Excel 文件至少需要包含表头和数据行");
    }

    List<String> titles = Arrays.stream(rows.get(0))
        .map(x -> StringUtils.remove(stringSafe(x), "*")).collect(Collectors.toList());
    String[] dataRow = rows.get(1);

    return parseTemplateContentFromRow(titles, dataRow, templateType);
  }

  /**
   * Parses CSV file and converts to TemplateContent based on template type.
   */
  public static TemplateContent parseCsvFile(MultipartFile file, TemplateType templateType) {
    List<String[]> rows = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }
        String[] values = parseCsvLine(line);
        rows.add(values);
      }
    } catch (IOException e) {
      throw ProtocolException.of("读取 CSV 文件失败: " + e.getMessage());
    }

    assertNotEmpty(rows, "CSV 文件内容为空");
    if (rows.size() < 2) {
      throw ProtocolException.of("CSV 文件至少需要包含表头和数据行");
    }

    List<String> titles = Arrays.stream(rows.get(0))
        .map(x -> StringUtils.remove(stringSafe(x), "*")).collect(Collectors.toList());
    String[] dataRow = rows.get(1);

    return parseTemplateContentFromRow(titles, dataRow, templateType);
  }

  /**
   * Parses CSV line handling quoted values.
   */
  public static String[] parseCsvLine(String line) {
    List<String> values = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder current = new StringBuilder();

    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ',' && !inQuotes) {
        values.add(current.toString().trim());
        current = new StringBuilder();
      } else {
        current.append(c);
      }
    }
    values.add(current.toString().trim());
    return values.toArray(new String[0]);
  }

  /**
   * Parses JSON file and converts to TemplateContent based on template type.
   */
  public static TemplateContent parseJsonFile(MultipartFile file, TemplateType templateType) {
    try {
      String content = new String(file.getBytes(), StandardCharsets.UTF_8);
      Map<String, Object> json = JsonUtils.convert(content,
          new TypeReference<Map<String, Object>>() {
          });

      return parseTemplateContentFromJson(json, templateType);
    } catch (IOException e) {
      throw ProtocolException.of("读取 JSON 文件失败: " + e.getMessage());
    }
  }

  /**
   * Parses TemplateContent from row data based on template type.
   */
  public static TemplateContent parseTemplateContentFromRow(List<String> titles, String[] dataRow,
      TemplateType templateType) {
    Locale zhLocale = zh_CN.toLocale();
    switch (templateType) {
      case ISSUE:
        return parseIssueTemplateContent(titles, dataRow, zhLocale);
      case TEST_PLAN:
        return parsePlanTemplateContent(titles, dataRow);
      case TEST_CASE:
        return parseCaseTemplateContent(titles, dataRow, zhLocale);
      default:
        throw ProtocolException.of("不支持的模版类型: " + templateType);
    }
  }

  /**
   * Parses TemplateContent from JSON data based on template type.
   */
  public static TemplateContent parseTemplateContentFromJson(Map<String, Object> json,
      TemplateType templateType) {
    Locale zhLocale = zh_CN.toLocale();
    switch (templateType) {
      case ISSUE:
        return parseIssueTemplateContentFromJson(json, zhLocale);
      case TEST_PLAN:
        return parsePlanTemplateContentFromJson(json);
      case TEST_CASE:
        return parseCaseTemplateContentFromJson(json, zhLocale);
      default:
        throw ProtocolException.of("不支持的模版类型: " + templateType);
    }
  }

  /**
   * Parses IssueTemplateContent from row data.
   */
  public static IssueTemplateContent parseIssueTemplateContent(List<String> titles, String[] dataRow,
      Locale locale) {
    IssueTemplateContent content = new IssueTemplateContent();

    int idx = getColumnIndex(titles, "任务类型");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setTaskType(TaskType.ofMessage(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "缺陷等级");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setBugLevel(BugLevel.ofMessage(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "优先级");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setPriority(Priority.ofMessage(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "是否漏测缺陷");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setMissingBug("是".equals(dataRow[idx]) || "true".equalsIgnoreCase(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "评估工作量方法");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setEvalWorkloadMethod(parseEvalWorkloadMethod(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "评估工作量");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      try {
        content.setEvalWorkload(new BigDecimal(dataRow[idx]));
      } catch (NumberFormatException e) {
        // Ignore invalid number
      }
    }

    idx = getColumnIndex(titles, "实际工作量");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      try {
        content.setActualWorkload(new BigDecimal(dataRow[idx]));
      } catch (NumberFormatException e) {
        // Ignore invalid number
      }
    }

    idx = getColumnIndex(titles, "描述");
    if (idx != -1 && idx < dataRow.length) {
      content.setDescription(stringSafe(dataRow[idx]));
    }

    return content;
  }

  /**
   * Parses PlanTemplateContent from row data.
   */
  public static PlanTemplateContent parsePlanTemplateContent(List<String> titles, String[] dataRow) {
    PlanTemplateContent content = new PlanTemplateContent();

    int idx = getColumnIndex(titles, "测试范围");
    if (idx != -1 && idx < dataRow.length) {
      content.setTestingScope(stringSafe(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "测试目标");
    if (idx != -1 && idx < dataRow.length) {
      content.setTestingObjectives(stringSafe(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "验收标准");
    if (idx != -1 && idx < dataRow.length) {
      content.setAcceptanceCriteria(stringSafe(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "其他信息");
    if (idx != -1 && idx < dataRow.length) {
      content.setOtherInformation(stringSafe(dataRow[idx]));
    }
    return content;
  }

  /**
   * Parses CaseTemplateContent from row data.
   */
  public static CaseTemplateContent parseCaseTemplateContent(List<String> titles, String[] dataRow,
      Locale locale) {
    CaseTemplateContent content = new CaseTemplateContent();

    int idx = getColumnIndex(titles, "测试层级");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setTestLayer(parseTestLayer(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "测试目的");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setTestPurpose(parseTestPurpose(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "前置条件");
    if (idx != -1 && idx < dataRow.length) {
      content.setPrecondition(stringSafe(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "步骤视图");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setStepView(parseCaseStepView(dataRow[idx], locale));
    }

    idx = getColumnIndex(titles, "测试步骤");
    if (idx != -1 && idx < dataRow.length && isNotEmpty(dataRow[idx])) {
      content.setSteps(parseSteps(dataRow[idx]));
    }

    idx = getColumnIndex(titles, "描述");
    if (idx != -1 && idx < dataRow.length) {
      content.setDescription(stringSafe(dataRow[idx]));
    }
    return content;
  }

  /**
   * Parses IssueTemplateContent from JSON data.
   */
  public static IssueTemplateContent parseIssueTemplateContentFromJson(Map<String, Object> json,
      Locale locale) {
    IssueTemplateContent content = new IssueTemplateContent();

    if (json.containsKey("taskType")) {
      content.setTaskType(TaskType.ofMessage(stringSafe(json.get("taskType")), locale));
    }
    if (json.containsKey("bugLevel")) {
      content.setBugLevel(BugLevel.ofMessage(stringSafe(json.get("bugLevel")), locale));
    }
    if (json.containsKey("priority")) {
      content.setPriority(Priority.ofMessage(stringSafe(json.get("priority")), locale));
    }
    if (json.containsKey("missingBug")) {
      Object val = json.get("missingBug");
      content.setMissingBug(val instanceof Boolean ? (Boolean) val
          : "true".equalsIgnoreCase(stringSafe(val)));
    }
    if (json.containsKey("evalWorkloadMethod")) {
      content.setEvalWorkloadMethod(
          parseEvalWorkloadMethod(stringSafe(json.get("evalWorkloadMethod")), locale));
    }
    if (json.containsKey("evalWorkload")) {
      Object val = json.get("evalWorkload");
      if (val instanceof Number) {
        content.setEvalWorkload(BigDecimal.valueOf(((Number) val).doubleValue()));
      }
    }
    if (json.containsKey("actualWorkload")) {
      Object val = json.get("actualWorkload");
      if (val instanceof Number) {
        content.setActualWorkload(BigDecimal.valueOf(((Number) val).doubleValue()));
      }
    }
    if (json.containsKey("description")) {
      content.setDescription(stringSafe(json.get("description")));
    }
    return content;
  }

  /**
   * Parses PlanTemplateContent from JSON data.
   */
  public static PlanTemplateContent parsePlanTemplateContentFromJson(Map<String, Object> json) {
    PlanTemplateContent content = new PlanTemplateContent();

    if (json.containsKey("testingScope")) {
      content.setTestingScope(stringSafe(json.get("testingScope")));
    }
    if (json.containsKey("testingObjectives")) {
      content.setTestingObjectives(stringSafe(json.get("testingObjectives")));
    }
    if (json.containsKey("acceptanceCriteria")) {
      content.setAcceptanceCriteria(stringSafe(json.get("acceptanceCriteria")));
    }
    if (json.containsKey("otherInformation")) {
      content.setOtherInformation(stringSafe(json.get("otherInformation")));
    }
    return content;
  }

  /**
   * Parses CaseTemplateContent from JSON data.
   */
  public static CaseTemplateContent parseCaseTemplateContentFromJson(Map<String, Object> json,
      Locale locale) {
    CaseTemplateContent content = new CaseTemplateContent();

    if (json.containsKey("testLayer")) {
      content.setTestLayer(parseTestLayer(stringSafe(json.get("testLayer")), locale));
    }
    if (json.containsKey("testPurpose")) {
      content.setTestPurpose(parseTestPurpose(stringSafe(json.get("testPurpose")), locale));
    }
    if (json.containsKey("precondition")) {
      content.setPrecondition(stringSafe(json.get("precondition")));
    }
    if (json.containsKey("stepView")) {
      content.setStepView(parseCaseStepView(stringSafe(json.get("stepView")), locale));
    }
    if (json.containsKey("steps")) {
      Object stepsObj = json.get("steps");
      if (stepsObj instanceof String) {
        content.setSteps(parseSteps((String) stepsObj));
      } else if (stepsObj instanceof List) {
        // If it's already a list, convert it
        try {
          content.setSteps(JsonUtils.convert(JsonUtils.toJson(stepsObj),
              new TypeReference<List<CaseTestStep>>() {
              }));
        } catch (Exception e) {
          throw ProtocolException.of("解析测试步骤失败: " + e.getMessage());
        }
      }
    }
    if (json.containsKey("description")) {
      content.setDescription(stringSafe(json.get("description")));
    }

    return content;
  }

  /**
   * Gets column index by column name (case-insensitive).
   */
  public static int getColumnIndex(List<String> titles, String columnName) {
    for (int i = 0; i < titles.size(); i++) {
      if (columnName.equalsIgnoreCase(titles.get(i))
          || titles.get(i).contains(columnName)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Parses steps string to List<CaseTestStep>. Format: step1##expected1@@step2##expected2
   */
  public static List<CaseTestStep> parseSteps(String steps) {
    if (isEmpty(steps)) {
      return null;
    }
    // Delete import step line break
    steps = steps.replaceAll("@@\n", "@@");
    String[] stepsArray = steps.split("@@");
    if (isEmpty(stepsArray)) {
      return null;
    }
    List<CaseTestStep> testSteps = new ArrayList<>();
    for (String step : stepsArray) {
      String[] stepArray = step.split("##");
      if (stepArray.length > 0) {
        testSteps.add(new CaseTestStep().setStep(stepArray[0])
            .setExpectedResult(stepArray.length > 1 ? stepArray[1] : null));
      }
    }
    return testSteps;
  }

  /**
   * Parses EvalWorkloadMethod from string (supports both enum name and message).
   */
  public static EvalWorkloadMethod parseEvalWorkloadMethod(String value, Locale locale) {
    if (isEmpty(value)) {
      return null;
    }
    try {
      // Try enum name first
      return EvalWorkloadMethod.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to find by message
      for (EvalWorkloadMethod method : EvalWorkloadMethod.values()) {
        String message = MessageHolder.message(method.getMessageKey(), locale);
        if (message.equals(value.trim())) {
          return method;
        }
      }
      throw ProtocolException.of("无效的评估工作量方法: " + value);
    }
  }

  /**
   * Parses TestLayer from string (supports both enum name and message).
   */
  public static TestLayer parseTestLayer(String value, Locale locale) {
    if (isEmpty(value)) {
      return null;
    }
    try {
      // Try enum name first
      return TestLayer.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to find by message
      for (TestLayer layer : TestLayer.values()) {
        String message = MessageHolder.message("xcm.enum.TestLayer." + layer.name(), locale);
        if (message.equals(value.trim())) {
          return layer;
        }
      }
      throw ProtocolException.of("无效的测试层级: " + value);
    }
  }

  /**
   * Parses TestPurpose from string (supports both enum name and message).
   */
  public static TestPurpose parseTestPurpose(String value, Locale locale) {
    if (isEmpty(value)) {
      return null;
    }
    try {
      // Try enum name first
      return TestPurpose.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to find by message
      for (TestPurpose purpose : TestPurpose.values()) {
        String message = MessageHolder.message("xcm.enum.TestPurpose." + purpose.name(), locale);
        if (message.equals(value.trim())) {
          return purpose;
        }
      }
      throw ProtocolException.of("无效的测试目的: " + value);
    }
  }

  /**
   * Parses CaseStepView from string (supports both enum name and message).
   */
  public static CaseStepView parseCaseStepView(String value, Locale locale) {
    if (isEmpty(value)) {
      return null;
    }
    try {
      // Try enum name first
      return CaseStepView.valueOf(value.toUpperCase());
    } catch (IllegalArgumentException e) {
      // Try to find by message
      for (CaseStepView view : CaseStepView.values()) {
        String message = MessageHolder.message("xcm.enum.CaseStepView." + view.name(), locale);
        if (message.equals(value.trim())) {
          return view;
        }
      }
      throw ProtocolException.of("无效的步骤视图: " + value);
    }
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
      writer.write(String.join(",", headers.stream().map(TemplateConverter::escapeCsv).toList()) + "\n");
      
      // Write data
      writer.write(String.join(",", data.stream().map(TemplateConverter::escapeCsv).toList()) + "\n");
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
  public static List<String> getTemplateContentHeaders(TemplateType templateType) {
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
  public static List<String> getTemplateContentData(TemplateContent templateContent, TemplateType templateType) {
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
                .collect(Collectors.joining("@@"));
          }
          data.add(stepsStr);
          data.add(caseContent.getDescription() != null ? caseContent.getDescription() : "");
        }
        break;
    }
    
    return data;
  }

  /**
   * Escapes CSV special characters.
   */
  public static String escapeCsv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
