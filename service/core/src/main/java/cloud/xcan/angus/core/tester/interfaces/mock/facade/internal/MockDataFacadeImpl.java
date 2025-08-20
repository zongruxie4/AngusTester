package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import cloud.xcan.angus.core.tester.application.cmd.mock.MockDataCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockDataQuery;
import cloud.xcan.angus.core.tester.domain.mock.data.MockFuncData;
import cloud.xcan.angus.core.tester.domain.mock.data.MockTextData;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockDataFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataScriptGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockDataAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockTextBatchVo;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockDataFacadeImpl implements MockDataFacade {

  @Resource
  private MockDataCmd mockDataCmd;

  @Resource
  private MockDataQuery mockDataQuery;

  @Override
  public List<Object> mockFunc(String mockFunc, int iterations) {
    return mockDataCmd.mockFunc(mockFunc, iterations);
  }

  @Override
  public List<MockBatchFuncVo> mockFuncInBatch(List<MockFuncDataDto> dto) {
    List<MockFuncData> mockFunctions = mockDataCmd.mockFuncInBatch(
        dto.stream().map(MockDataAssembler::toMockFuncRequest).toList());
    return mockFunctions.stream().map(MockDataAssembler::toMockFuncDataVo)
        .toList();
  }

  @Override
  public List<String> mockText(String text, int iterations) {
    return mockDataCmd.mockText(text, iterations);
  }

  @Override
  public List<MockTextBatchVo> mockTextInBatch(List<MockTextDataDto> dto) {
    List<MockTextData> mockFunctions = mockDataCmd.mockTextInBatch(
        dto.stream().map(MockDataAssembler::toMockTextRequest).toList());
    return mockFunctions.stream().map(MockDataAssembler::toMockTextBatchVo)
        .toList();
  }

  @Override
  public IdKey<Long, Object> dataScriptGen(MockDataScriptGenDto dto) {
    return mockDataCmd.dataScriptAdd(dto.getScriptId(), dto.getProjectId(), dto.getPlugin(),
        dto.getConfiguration(), dto.getMockData());
  }

  @Override
  public String dataScriptView(MockDataScriptGenDto dto) {
    return mockDataCmd.dataScriptView(dto.getPlugin(), dto.getConfiguration(), dto.getMockData());
  }

  @Override
  public IdKey<Long, Object> dataGen(MockDataGenDto dto) {
    return mockDataCmd.dataGen(dto.getScriptId(), dto.getProjectId(), dto.getPlugin(),
        dto.getConfiguration(), dto.getMockData());
  }

  @Override
  public List<MockFunction> allFunctions() {
    return mockDataQuery.allFunctions();
  }

  @Override
  public List<MockFunction> allFunctionsReload() {
    return mockDataQuery.allFunctionsReload();
  }
}




