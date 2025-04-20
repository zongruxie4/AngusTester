package cloud.xcan.angus.core.tester.interfaces.mock;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_MOCK_FUNC_ITERATIONS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_MOCK_FUNC_LENGTH;
import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_MOCK_TEXT_ITERATIONS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_MOCK_TEXT_LENGTH;

import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockDataFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockDataScriptGenDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockFuncDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.data.MockTextDataDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockBatchFuncVo;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data.MockTextBatchVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.jmock.core.parser.docs.model.MockFunction;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
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

  @Operation(description = "Generate data based on mock function expressions", operationId = "mock:function:data")
  @ApiResponse(responseCode = "200", description = "Successfully generated data")
  @PostMapping(value = "/function/data")
  public ApiLocaleResult<List<Object>> mockFunc(
      @Valid @Length(max = MAX_MOCK_FUNC_LENGTH) @RequestParam("function") @Parameter(name = "function", description = "Mock function expression", required = true) String function,
      @Valid @Min(1) @Max(MAX_MOCK_FUNC_ITERATIONS) @RequestParam("iterations") @Parameter(name = "iterations", description = "The number of iteration. For more iterations, please use 'EXECUTE' to generate data", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockFunc(function, iterations));
  }

  @Operation(description = "Generate data in batch based on mock function expressions", operationId = "mock:function:batch:data")
  @ApiResponse(responseCode = "200", description = "Successfully generated data")
  @PostMapping(value = "/function/data/batch")
  public ApiLocaleResult<List<MockBatchFuncVo>> mockFuncInBatch(
      @Valid @RequestBody List<MockFuncDataDto> dtos) {
    return ApiLocaleResult.success(mockDataFacade.mockFuncInBatch(dtos));
  }

  @Operation(description = "Replace function expressions in text", operationId = "mock:text:data")
  @ApiResponse(responseCode = "200", description = "Successfully generated data")
  @PostMapping(value = "/text/data")
  public ApiLocaleResult<List<String>> mockText(
      @Valid @Length(max = MAX_MOCK_TEXT_LENGTH) @RequestParam("text") @Parameter(name = "text", description = "Mock text", required = true) String text,
      @Valid @Min(1) @Max(MAX_MOCK_TEXT_ITERATIONS) @RequestParam("iterations") @Parameter(name = "iterations", description = "The number of iteration. For more iterations, please use 'EXECUTE' to generate data", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockText(text, iterations));
  }

  @Operation(description = "Batch replace function expressions in text", operationId = "mock:text:batch:data")
  @ApiResponse(responseCode = "200", description = "Successfully generated data")
  @PostMapping(value = "/text/data/batch")
  public ApiLocaleResult<List<MockTextBatchVo>> mockTextInBatch(
      @Valid @RequestBody List<MockTextDataDto> dtos) {
    return ApiLocaleResult.success(mockDataFacade.mockTextInBatch(dtos));
  }

  @Operation(description = "Generate mock data script", operationId = "mock:data:script:generate")
  @ApiResponse(responseCode = "200", description = "Successfully generated script")
  @PostMapping(value = "/data/script")
  public ApiLocaleResult<IdKey<Long, Object>> dataScriptGen(
      @Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataScriptGen(dto));
  }

  @Operation(description = "View mock data script", operationId = "mock:data:script:content:view")
  @ApiResponse(responseCode = "200", description = "Successfully generated script")
  @PostMapping(value = "/data/script/content")
  public ApiLocaleResult<String> dataScriptView(@Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.successData(mockDataFacade.dataScriptView(dto));
  }

  @Operation(description = "Create execution and generate data", operationId = "mock:data:execution:generate")
  @ApiResponse(responseCode = "200", description = "Successfully created execution")
  @PostMapping(value = "/data/execution")
  public ApiLocaleResult<IdKey<Long, Object>> dataGen(@Valid @RequestBody MockDataGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataGen(dto));
  }

  @Operation(description = "Query all mock functions", operationId = "mock:function:all")
  @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  @GetMapping(value = "/functions")
  public ApiLocaleResult<List<MockFunction>> allFunctions() {
    return ApiLocaleResult.success(mockDataFacade.allFunctions());
  }

  @Operation(description = "Query and reload all mock functions", operationId = "mock:function:all:reload")
  @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  @GetMapping(value = "/functions/reload")
  public ApiLocaleResult<List<MockFunction>> allFunctionsReload() {
    return ApiLocaleResult.success(mockDataFacade.allFunctionsReload());
  }
}
