package cloud.xcan.angus.core.tester.application.query.test.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.test.TestTemplateQuery;
import cloud.xcan.angus.core.tester.domain.test.template.TestTemplate;
import cloud.xcan.angus.core.tester.domain.test.template.TestTemplateRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Query implementation for test template queries.
 * <p>
 * Provides query operations for retrieving test templates.
 */
@Biz
public class TestTemplateQueryImpl implements TestTemplateQuery {

  @Resource
  private TestTemplateRepo testTemplateRepo;

  /**
   * Finds a test template by ID with validation.
   * <p>
   * Retrieves a template and throws ResourceNotFound if not found.
   *
   * @param id the template ID
   * @return TestTemplate object
   * @throws ResourceNotFound if template is not found
   */
  @Override
  public TestTemplate checkAndFind(Long id) {
    return new BizTemplate<TestTemplate>() {
      @Override
      protected TestTemplate process() {
        return testTemplateRepo.findById(id)
            .orElseThrow(() -> ResourceNotFound.of(id, "TestTemplate"));
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
  public List<TestTemplate> findAll() {
    return new BizTemplate<List<TestTemplate>>() {
      @Override
      protected List<TestTemplate> process() {
        List<TestTemplate> templates = new ArrayList<>();
        List<TestTemplate> tenantTemplates = testTemplateRepo.findByIsSystem(false);
        templates.addAll(tenantTemplates);

        closeMultiTenantCtrl();
        List<TestTemplate> systemTemplates = testTemplateRepo.findByIsSystem(true);
        enableMultiTenantCtrl();
        templates.addAll(systemTemplates);
        return templates;
      }
    }.execute();
  }
}

