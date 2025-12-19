package cloud.xcan.angus.core.tester.application.query.mock.impl;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.mock.MockDataQuery;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.jmock.core.parser.MockFunctionDocParser;
import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import jakarta.annotation.Resource;
import java.util.List;

/**
 * Implementation of MockDataQuery for managing Mock function operations and data retrieval.
 * <p>
 * This class provides functionality for querying and managing Mock functions, which define data
 * generation and manipulation capabilities for mock services. It handles function loading,
 * reloading, and language-specific function retrieval.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Mock function retrieval with language-specific loading</li>
 *   <li>Function cache reloading for dynamic updates</li>
 *   <li>Default language support for function documentation</li>
 *   <li>Efficient function parsing and caching</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 */
@Service
public class MockDataQueryImpl implements MockDataQuery {

  @Resource
  private MockFunctionDocParser mockFunctionDocParser;

  /**
   * Retrieves all available Mock functions for the current user's default language.
   * <p>
   * Loads Mock functions from the documentation parser using the user's preferred language.
   * Functions are cached for performance and include data generation capabilities, validation
   * rules, and transformation logic for mock service responses.
   * <p>
   * The method uses the default language setting from the principal context to ensure localized
   * function documentation and behavior.
   *
   * @return List of MockFunction objects with complete function definitions
   */
  @Override
  public List<MockFunction> allFunctions() {
    return new BizTemplate<List<MockFunction>>() {

      @Override
      protected List<MockFunction> process() {
        // Load Mock functions using the user's default language setting
        return mockFunctionDocParser.load(PrincipalContext.getDefaultLanguage());
      }
    }.execute();
  }

  /**
   * Reloads all Mock functions from the documentation parser, refreshing the cache.
   * <p>
   * Forces a complete reload of Mock functions, clearing any cached data and re-parsing function
   * documentation. This is useful when function definitions have been updated and the cache needs
   * to be refreshed.
   * <p>
   * The method uses the default language setting from the principal context to ensure consistent
   * language-specific function loading.
   *
   * @return List of MockFunction objects with refreshed function definitions
   */
  @Override
  public List<MockFunction> allFunctionsReload() {
    return new BizTemplate<List<MockFunction>>() {

      @Override
      protected List<MockFunction> process() {
        // Reload Mock functions using the user's default language setting
        return mockFunctionDocParser.reload(PrincipalContext.getDefaultLanguage());
      }
    }.execute();
  }
}
