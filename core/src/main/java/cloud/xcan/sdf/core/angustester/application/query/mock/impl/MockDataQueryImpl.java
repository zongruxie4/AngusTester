package cloud.xcan.sdf.core.angustester.application.query.mock.impl;

import cloud.xcan.comp.jmock.core.parser.MockFunctionDocParser;
import cloud.xcan.comp.jmock.core.parser.docs.model.MockFunction;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockDataQuery;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import java.util.List;
import javax.annotation.Resource;

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
