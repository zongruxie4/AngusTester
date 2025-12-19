package cloud.xcan.angus.core.tester.application.query.project.impl;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.project.TemplateQuery;
import cloud.xcan.angus.core.tester.domain.project.template.Template;
import cloud.xcan.angus.core.tester.domain.project.template.TemplateRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Query implementation for test template queries.
 * <p>
 * Provides query operations for retrieving test templates.
 */
@Service
public class TemplateQueryImpl implements TemplateQuery {

  @Resource
  private TemplateRepo templateRepo;

  /**
   * Finds a test template by ID with validation.
   * <p>
   * Retrieves a template and throws ResourceNotFound if not found.
   *
   * @param id the template ID
   * @return Template object
   * @throws ResourceNotFound if template is not found
   */
  @Override
  public Template checkAndFind(Long id) {
    return new BizTemplate<Template>() {
      @Override
      protected Template process() {
        return templateRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "Template"));
      }
    }.execute();
  }

  /**
   * Finds all test templates.
   * <p>
   * Retrieves all templates (both system and custom templates).
   *
   * @return List of all test templates
   */
  @Override
  public List<Template> findAll() {
    return new BizTemplate<List<Template>>() {
      @Override
      protected List<Template> process() {
        List<Template> templates = new ArrayList<>();
        List<Template> tenantTemplates = templateRepo.findByIsSystem(false);
        templates.addAll(tenantTemplates);

        closeMultiTenantCtrl();
        List<Template> systemTemplates = templateRepo.findByIsSystem(true);
        enableMultiTenantCtrl();
        templates.addAll(systemTemplates);
        return templates;
      }
    }.execute();
  }
}

