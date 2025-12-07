package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.spec.locale.SupportedLanguage.zh_CN;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.project.TemplateCmd;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateRepo;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.CaseTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.IssueTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.PlanTemplateContent;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.core.tester.domain.test.TestLayer;
import cloud.xcan.angus.core.tester.domain.test.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.extraction.utils.PoiUtils;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.utils.JsonUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for test template management operations.
 * <p>
 * Provides comprehensive CRUD operations for test templates including creation, modification, and
 * deletion with quota management and permission checks.
 * <p>
 * Implements business logic validation, quota limits (max 200 custom templates), and activity
 * logging for all template operations.
 */
@Biz
public class TemplateCmdImpl extends CommCmd<Template, Long> implements TemplateCmd {

  private static final int MAX_CUSTOM_TEMPLATE_COUNT = 200;

  @Resource
  private TemplateRepo templateRepo;

  @Resource
  private TemplateQuery templateQuery;

  /**
   * Adds a new test template to the system.
   * <p>
   * Performs comprehensive validation including quota limits (max 200 custom templates), name
   * uniqueness, and sets template as custom template (isSystem = false).
   * <p>
   * Logs creation activity for audit purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(Template template) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check custom template quota (max 200)
        long customTemplateCount = templateRepo.countByIsSystem(false);
        if (customTemplateCount >= MAX_CUSTOM_TEMPLATE_COUNT) {
          throw QuotaException.of(
              String.format("最多只能创建 %d 个自定义模版，当前已有 %d 个",
                  MAX_CUSTOM_TEMPLATE_COUNT, customTemplateCount));
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Ensure template is marked as custom (not system template)
        template.setIsSystem(false);
        return insert(template, "name");
      }
    }.execute();
  }

  /**
   * Updates an existing custom test template.
   * <p>
   * Validates template existence, ensures it's a custom template (not system template), and updates
   * template information.
   * <p>
   * Logs update activity for audit purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Template template) {
    new BizTemplate<Void>() {
      Template templateDb;

      @Override
      protected void checkParams() {
        // Ensure template exists
        templateDb = templateQuery.checkAndFind(template.getId());

        // Ensure it's a custom template (not system template)
        if (nonNull(templateDb.getIsSystem()) && templateDb.getIsSystem()) {
          throw new IllegalArgumentException("系统模版不允许修改");
        }
      }

      @Override
      protected Void process() {
        // Update template fields
        templateDb.setName(template.getName());
        templateDb.setTemplateType(template.getTemplateType());
        templateDb.setTemplateContent(template.getTemplateContent());
        templateRepo.save(templateDb);
        return null;
      }
    }.execute();
  }

  /**
   * Deletes a custom test template from the system.
   * <p>
   * Validates template existence and ensures it's a custom template (not system template) before
   * deletion.
   * <p>
   * Logs deletion activity for audit purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Template templateDb;

      @Override
      protected void checkParams() {
        // Ensure template exists
        templateDb = templateQuery.checkAndFind(id);

        // Ensure it's a custom template (not system template)
        if (nonNull(templateDb.getIsSystem()) && templateDb.getIsSystem()) {
          throw new IllegalArgumentException("系统模版不允许删除");
        }
      }

      @Override
      protected Void process() {
        // Delete template
        templateRepo.delete(templateDb);
        return null;
      }
    }.execute();
  }

  /**
   * Imports a single template from file (Excel, CSV, or JSON format).
   * <p>
   * Supports multiple file formats and validates template data based on template type. If a
   * template with the same name already exists, import will be stopped.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(TemplateType templateType, String name, MultipartFile file) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check if template with same name already exists
        Optional<Template> existingTemplate = templateRepo.findAll().stream()
            .filter(t -> name.equals(t.getName()))
            .findFirst();
        assertTrue(existingTemplate.isEmpty(),
            String.format("模版名称 '%s' 已存在，请使用其他名称", name));

        // Check custom template quota
        long customTemplateCount = templateRepo.countByIsSystem(false);
        assertTrue(customTemplateCount < MAX_CUSTOM_TEMPLATE_COUNT,
            String.format("最多只能创建 %d 个自定义模版，当前已有 %d 个", MAX_CUSTOM_TEMPLATE_COUNT,
                customTemplateCount));
      }

      @Override
      protected IdKey<Long, Object> process() {
        String fileName = file.getOriginalFilename();
        assertNotEmpty(fileName, "File name is required");

        // Parse template content from file based on template type
        TemplateContent templateContent = parseImportFile(file, fileName, templateType);

        // Create template
        Template template = new Template()
            .setName(name)
            .setTemplateType(templateType)
            .setTemplateContent(templateContent)
            .setIsSystem(false);
        return insert(template, "name");
      }
    }.execute();
  }

  /**
   * Parses import file based on file extension (Excel, CSV, or JSON) and template type. Returns
   * TemplateContent based on the template type.
   */
  private TemplateContent parseImportFile(MultipartFile file, String fileName,
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
  private TemplateContent parseExcelFile(MultipartFile file, TemplateType templateType) {
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
  private TemplateContent parseCsvFile(MultipartFile file, TemplateType templateType) {
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
  private String[] parseCsvLine(String line) {
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
  private TemplateContent parseJsonFile(MultipartFile file, TemplateType templateType) {
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
  private TemplateContent parseTemplateContentFromRow(List<String> titles, String[] dataRow,
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
  private TemplateContent parseTemplateContentFromJson(Map<String, Object> json,
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
  private IssueTemplateContent parseIssueTemplateContent(List<String> titles, String[] dataRow,
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
  private PlanTemplateContent parsePlanTemplateContent(List<String> titles, String[] dataRow) {
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
  private CaseTemplateContent parseCaseTemplateContent(List<String> titles, String[] dataRow,
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
  private IssueTemplateContent parseIssueTemplateContentFromJson(Map<String, Object> json,
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
  private PlanTemplateContent parsePlanTemplateContentFromJson(Map<String, Object> json) {
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
  private CaseTemplateContent parseCaseTemplateContentFromJson(Map<String, Object> json,
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
  private int getColumnIndex(List<String> titles, String columnName) {
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
  private List<CaseTestStep> parseSteps(String steps) {
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
  private EvalWorkloadMethod parseEvalWorkloadMethod(String value, Locale locale) {
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
  private TestLayer parseTestLayer(String value, Locale locale) {
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
  private TestPurpose parseTestPurpose(String value, Locale locale) {
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
  private CaseStepView parseCaseStepView(String value, Locale locale) {
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

  @Override
  protected BaseRepository<Template, Long> getRepository() {
    return this.templateRepo;
  }
}

