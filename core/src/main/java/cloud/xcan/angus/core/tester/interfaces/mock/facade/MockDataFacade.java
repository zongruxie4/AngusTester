package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataScriptGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockTextBatchVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import java.util.List;

public interface MockDataFacade {

  List<Object> mockFunc(String mockFunc, int iterations);

  List<MockBatchFuncVo> mockFuncInBatch(List<MockFuncDataDto> dto);

  List<String> mockText(String text, int iterations);

  List<MockTextBatchVo> mockTextInBatch(List<MockTextDataDto> dto);

  IdKey<Long, Object> dataScriptGen(MockDataScriptGenDto dto);

  String dataScriptView(MockDataScriptGenDto dto);

  IdKey<Long, Object> dataGen(MockDataGenDto dto);

  List<MockFunction> allFunctions();

  List<MockFunction> allFunctionsReload();
}
