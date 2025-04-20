package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import java.util.List;

public interface MockDataQuery {

  List<MockFunction> allFunctions();

  List<MockFunction> allFunctionsReload();

}
