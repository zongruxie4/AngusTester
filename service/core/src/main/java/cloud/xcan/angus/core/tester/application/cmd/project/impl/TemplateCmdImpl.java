package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.project.TemplateCmd;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for test template management operations.
 * <p>
 * Provides comprehensive CRUD operations for test templates including creation, modification,
 * and deletion with quota management and permission checks.
 * <p>
 * Implements business logic validation, quota limits (max 200 custom templates),
 * and activity logging for all template operations.
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
   * Performs comprehensive validation including quota limits (max 200 custom templates),
   * name uniqueness, and sets template as custom template (isSystem = false).
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
              String.format("最多只能创建 %d 个自定义模版，当前已有 %d 个", MAX_CUSTOM_TEMPLATE_COUNT, customTemplateCount));
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
   * Validates template existence, ensures it's a custom template (not system template),
   * and updates template information.
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
   * Validates template existence and ensures it's a custom template (not system template)
   * before deletion.
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

  @Override
  protected BaseRepository<Template, Long> getRepository() {
    return this.templateRepo;
  }
}

