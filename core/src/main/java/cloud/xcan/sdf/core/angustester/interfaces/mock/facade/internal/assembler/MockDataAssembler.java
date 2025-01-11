package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler;

import cloud.xcan.sdf.core.angustester.domain.mock.data.MockFuncData;
import cloud.xcan.sdf.core.angustester.domain.mock.data.MockFuncRequest;
import cloud.xcan.sdf.core.angustester.domain.mock.data.MockTextData;
import cloud.xcan.sdf.core.angustester.domain.mock.data.MockTextRequest;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockTextBatchVo;

public class MockDataAssembler {

  public static MockFuncRequest toMockFuncRequest(MockFuncDataDto dto) {
    return new MockFuncRequest().setFunction(dto.getFunction())
        .setOutKey(dto.getOutKey())
        .setIterations(dto.getIterations());
  }

  public static MockBatchFuncVo toMockFuncDataVo(MockFuncData data) {
    return new MockBatchFuncVo().setName(data.getOutKey())
        .setValues(data.getValues());
  }

  public static MockTextBatchVo toMockTextBatchVo(MockTextData data) {
    return new MockTextBatchVo().setOutKey(data.getOutKey())
        .setValues(data.getValues());
  }

  public static MockTextRequest toMockTextRequest(MockTextDataDto dto) {
    return new MockTextRequest().setText(dto.getText())
        .setOutKey(dto.getOutKey())
        .setIterations(dto.getIterations());
  }

}