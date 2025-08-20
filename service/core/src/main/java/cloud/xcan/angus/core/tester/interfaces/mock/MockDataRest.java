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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Mock Data", description = "Mock Data Generation - Test data generation and debug dependency management via JMock rules with data simulation capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/mock")
public class MockDataRest {

  @Resource
  private MockDataFacade mockDataFacade;

  @Operation(summary = "Generate data using mock function expressions",
      description = "Generate test data based on JMock function expressions with configurable iterations for testing scenarios",
      operationId = "mock:function:data")
  @ApiResponse(responseCode = "200", description = "Mock function data generated successfully")
  @PostMapping(value = "/function/data")
  public ApiLocaleResult<List<Object>> mockFunc(
      @Valid @Length(max = MAX_MOCK_FUNC_LENGTH) @RequestParam("function") @Parameter(name = "function", description = "JMock function expression for data generation", required = true) String function,
      @Valid @Min(1) @Max(MAX_MOCK_FUNC_ITERATIONS) @RequestParam("iterations") @Parameter(name = "iterations", description = "Number of data iterations to generate with execution fallback for large volumes", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockFunc(function, iterations));
  }

  @Operation(summary = "Generate batch data using mock function expressions",
      description = "Generate multiple test data sets using JMock function expressions for batch testing scenarios",
      operationId = "mock:function:batch:data")
  @ApiResponse(responseCode = "200", description = "Batch mock function data generated successfully")
  @PostMapping(value = "/function/data/batch")
  public ApiLocaleResult<List<MockBatchFuncVo>> mockFuncInBatch(
      @Valid @RequestBody List<MockFuncDataDto> dto) {
    return ApiLocaleResult.success(mockDataFacade.mockFuncInBatch(dto));
  }

  @Operation(summary = "Replace function expressions in text content",
      description = "Generate text content with embedded JMock function expressions replaced with actual data for dynamic content generation",
      operationId = "mock:text:data")
  @ApiResponse(responseCode = "200", description = "Text content with function expressions replaced successfully")
  @PostMapping(value = "/text/data")
  public ApiLocaleResult<List<String>> mockText(
      @Valid @Length(max = MAX_MOCK_TEXT_LENGTH) @RequestParam("text") @Parameter(name = "text", description = "Text content with embedded JMock function expressions", required = true) String text,
      @Valid @Min(1) @Max(MAX_MOCK_TEXT_ITERATIONS) @RequestParam("iterations") @Parameter(name = "iterations", description = "Number of text iterations to generate with execution fallback for large volumes", required = true) int iterations) {
    return ApiLocaleResult.success(mockDataFacade.mockText(text, iterations));
  }

  @Operation(summary = "Batch replace function expressions in text content",
      description = "Process multiple text contents with embedded JMock function expressions for batch text generation",
      operationId = "mock:text:batch:data")
  @ApiResponse(responseCode = "200", description = "Batch text content with function expressions replaced successfully")
  @PostMapping(value = "/text/data/batch")
  public ApiLocaleResult<List<MockTextBatchVo>> mockTextInBatch(
      @Valid @RequestBody List<MockTextDataDto> dto) {
    return ApiLocaleResult.success(mockDataFacade.mockTextInBatch(dto));
  }

  @Operation(summary = "Generate mock data script",
      description = "Create executable script for mock data generation with configuration and automation capabilities",
      operationId = "mock:data:script:generate")
  @ApiResponse(responseCode = "200", description = "Mock data script generated successfully")
  @PostMapping(value = "/data/script")
  public ApiLocaleResult<IdKey<Long, Object>> dataScriptGen(
      @Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataScriptGen(dto));
  }

  @Operation(summary = "Preview mock data script content",
      description = "View generated mock data script content without execution for validation and review purposes",
      operationId = "mock:data:script:content:view")
  @ApiResponse(responseCode = "200", description = "Mock data script content previewed successfully")
  @PostMapping(value = "/data/script/content")
  public ApiLocaleResult<String> dataScriptView(@Valid @RequestBody MockDataScriptGenDto dto) {
    return ApiLocaleResult.successData(mockDataFacade.dataScriptView(dto));
  }

  @Operation(summary = "Create execution and generate data",
      description = "Execute mock data generation with execution management and result tracking",
      operationId = "mock:data:execution:generate")
  @ApiResponse(responseCode = "200", description = "Mock data execution created successfully")
  @PostMapping(value = "/data/execution")
  public ApiLocaleResult<IdKey<Long, Object>> dataGen(@Valid @RequestBody MockDataGenDto dto) {
    return ApiLocaleResult.success(mockDataFacade.dataGen(dto));
  }

  @Operation(summary = "Query all available mock functions",
      description = "Retrieve list of all available JMock functions for data generation reference and discovery",
      operationId = "mock:function:all")
  @ApiResponse(responseCode = "200", description = "All mock functions retrieved successfully")
  @GetMapping(value = "/functions")
  public ApiLocaleResult<List<MockFunction>> allFunctions() {
    return ApiLocaleResult.success(mockDataFacade.allFunctions());
  }

  @Operation(summary = "Query and reload all mock functions",
      description = "Reload and retrieve all available JMock functions for dynamic function discovery and updates",
      operationId = "mock:function:all:reload")
  @ApiResponse(responseCode = "200", description = "All mock functions reloaded and retrieved successfully")
  @GetMapping(value = "/functions/reload")
  public ApiLocaleResult<List<MockFunction>> allFunctionsReload() {
    return ApiLocaleResult.success(mockDataFacade.allFunctionsReload());
  }
}
