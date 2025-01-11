package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.comp.jmock.core.parser.docs.model.MockFunction;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockDataGenDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockDataScriptGenDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockTextBatchVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface MockDataFacade {

  List<Object> mockFunc(String mockFunc, int iterations);

  List<MockBatchFuncVo> mockFuncInBatch(List<MockFuncDataDto> dtos);

  List<String> mockText(String text, int iterations);

  List<MockTextBatchVo> mockTextInBatch(List<MockTextDataDto> dtos);

  IdKey<Long, Object> dataScriptGen(MockDataScriptGenDto dto);

  String dataScriptView(MockDataScriptGenDto dto);

  IdKey<Long, Object> dataGen(MockDataGenDto dto);

  List<MockFunction> allFunctions();

  List<MockFunction> allFunctionsReload();
}
