package cloud.xcan.angus.core.tester.application.query.mock.impl;

import cloud.xcan.jmock.core.parser.MockFunctionDocParser;
import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import cloud.xcan.angus.core.tester.application.query.mock.MockDataQuery;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.util.List;
import jakarta.annotation.Resource;

@Biz
public class MockDataQueryImpl implements MockDataQuery {

  @Resource
  private MockFunctionDocParser mockFunctionDocParser;

  @Override
  public List<MockFunction> allFunctions() {
    return new BizTemplate<List<MockFunction>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<MockFunction> process() {
        return mockFunctionDocParser.load(PrincipalContext.getDefaultLanguage());
      }
    }.execute();
  }

  @Override
  public List<MockFunction> allFunctionsReload() {
    return new BizTemplate<List<MockFunction>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected List<MockFunction> process() {
        return mockFunctionDocParser.reload(PrincipalContext.getDefaultLanguage());
      }
    }.execute();
  }
}
