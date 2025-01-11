package cloud.xcan.sdf.core.angustester.interfaces.mock;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_MOCK_FUNC_ITERATIONS;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_MOCK_FUNC_LENGTH;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_MOCK_TEXT_ITERATIONS;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_MOCK_TEXT_LENGTH;

import cloud.xcan.comp.jmock.core.parser.docs.model.MockFunction;
import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockDataFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockDataGenDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockDataScriptGenDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.data.MockTextBatchVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "MockData")
@Validated
@RestController
@RequestMapping("/api/v1/mock")
public class MockDataRest {

  @Resource
  private MockDataFacade mockDataFacade;

  @ApiOperation(value = "Generate data based on mock function expressions", nickname = "mock:function:data")
  @ApiResponse(code = 200, message = "Successfully generated data", response = ApiLocaleResult.class)
  @PostMapping(value = "/function/data")
  public ApiLocaleResult<List<Object>> mockFunc(
      @Valid @Length(max = MAX_MOCK_FUNC_LENGTH) @RequestParam("function") @ApiParam(name = "function", value = "Mock function expression", required = true) String function,
      @Valid @Min(1) @Max(MAX_MOCK_FUNC_ITERATIONS) @RequestParam("iterations") @ApiParam(name = "iterations", value = "The number of iteration. For more iterations, please use 'EXECUTE' to generate data", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockFunc(function, iterations));
  }

  @ApiOperation(value = "Generate data in batch based on mock function expressions", nickname = "mock:function:batch:data")
  @ApiResponse(code = 200, message = "Successfully generated data", response = ApiLocaleResult.class)
  @PostMapping(value = "/function/data/batch")
  public ApiLocaleResult<List<MockBatchFuncVo>> mockFuncInBatch(
      @Valid @RequestBody List<MockFuncDataDto> dtos) {
    return ApiLocaleResult.success(mockDataFacade.mockFuncInBatch(dtos));
  }

  @ApiOperation(value = "Replace function expressions in text", nickname = "mock:text:data")
  @ApiResponse(code = 200, message = "Successfully generated data", response = ApiLocaleResult.class)
  @PostMapping(value = "/text/data")
  public ApiLocaleResult<List<String>> mockText(
      @Valid @Length(max = MAX_MOCK_TEXT_LENGTH) @RequestParam("text") @ApiParam(name = "text", value = "Mock text", required = true) String text,
      @Valid @Min(1) @Max(MAX_MOCK_TEXT_ITERATIONS) @RequestParam("iterations") @ApiParam(name = "iterations", value = "The number of iteration. For more iterations, please use 'EXECUTE' to generate data", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockText(text, iterations));
  }

  @ApiOperation(value = "Batch replace function expressions in text", nickname = "mock:text:batch:data")
  @ApiResponse(code = 200, message = "Successfully generated data", response = ApiLocaleResult.class)
  @PostMapping(value = "/text/data/batch")
  public ApiLocaleResult<List<MockTextBatchVo>> mockTextInBatch(
      @Valid @RequestBody List<MockTextDataDto> dtos) {
    return ApiLocaleResult.success(mockDataFacade.mockTextInBatch(dtos));
  }

  @ApiOperation(value = "Generate mock data script", nickname = "mock:data:script:generate")
  @ApiResponse(code = 200, message = "Successfully generated script", response = ApiLocaleResult.class)
  @PostMapping(value = "/data/script")
  public ApiLocaleResult<IdKey<Long, Object>> dataScriptGen(
      @Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataScriptGen(dto));
  }

  @ApiOperation(value = "View mock data script", nickname = "mock:data:script:content:view")
  @ApiResponse(code = 200, message = "Successfully generated script", response = ApiLocaleResult.class)
  @PostMapping(value = "/data/script/content")
  public ApiLocaleResult<String> dataScriptView(@Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.successData(mockDataFacade.dataScriptView(dto));
  }

  @ApiOperation(value = "Create execution and generate data", nickname = "mock:data:execution:generate")
  @ApiResponse(code = 200, message = "Successfully created execution", response = ApiLocaleResult.class)
  @PostMapping(value = "/data/execution")
  public ApiLocaleResult<IdKey<Long, Object>> dataGen(@Valid @RequestBody MockDataGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataGen(dto));
  }

  @ApiOperation(value = "Query all mock functions", nickname = "mock:function:all")
  @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  @GetMapping(value = "/functions")
  public ApiLocaleResult<List<MockFunction>> allFunctions() {
    return ApiLocaleResult.success(mockDataFacade.allFunctions());
  }

  @ApiOperation(value = "Query and reload all mock functions", nickname = "mock:function:all:reload")
  @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  @GetMapping(value = "/functions/reload")
  public ApiLocaleResult<List<MockFunction>> allFunctionsReload() {
    return ApiLocaleResult.success(mockDataFacade.allFunctionsReload());
  }
}
