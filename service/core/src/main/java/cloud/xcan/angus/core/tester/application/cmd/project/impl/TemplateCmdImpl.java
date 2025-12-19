package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static java.util.Objects.nonNull;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.project.TemplateCmd;
import cloud.xcan.angus.core.tester.application.converter.TemplateConverter;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateRepo;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateType;
import cloud.xcan.angus.core.tester.domain.project.template.content.TemplateContent;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Optional;
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
@Service
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
        TemplateContent templateContent = TemplateConverter.parseImportFile(file, fileName,
            templateType);

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


  @Override
  protected BaseRepository<Template, Long> getRepository() {
    return this.templateRepo;
  }
}

