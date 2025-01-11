package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.comp.jmock.core.parser.docs.model.MockFunction;
import java.util.List;

public interface MockDataQuery {

  List<MockFunction> allFunctions();

  List<MockFunction> allFunctionsReload();

}
