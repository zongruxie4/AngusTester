package cloud.xcan.angus.core.tester.interfaces.project;

import cloud.xcan.angus.core.tester.interfaces.project.facade.EvaluationFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.EvaluationDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Evaluation", description = "Test Evaluation Management API - APIs for creating, updating, deleting, querying test evaluations and generating evaluation results")
@Validated
@RestController
@RequestMapping("/api/v1/project/evaluation")
public class EvaluationRest {

  @Resource
  private EvaluationFacade evaluationFacade;

  @Operation(summary = "Create test evaluation",
      description = "Create a new test evaluation with specified scope, purposes, and timeline",
      operationId = "evaluation:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Test evaluation created successfully")
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody EvaluationAddDto dto) {
    return ApiLocaleResult.success(evaluationFacade.add(dto));
  }

  @Operation(summary = "Update test evaluation",
      description = "Update an existing test evaluation configuration including scope, purposes, and timeline",
      operationId = "evaluation:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test evaluation updated successfully"),
      @ApiResponse(responseCode = "404", description = "Test evaluation not found")
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody EvaluationUpdateDto dto) {
    evaluationFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Generate evaluation result",
      description = "Calculate and generate evaluation results based on the evaluation configuration and current project data",
      operationId = "evaluation:generate:result")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Evaluation result generated successfully"),
      @ApiResponse(responseCode = "404", description = "Test evaluation not found")
  })
  @PostMapping("/{id}/result")
  public ApiLocaleResult<?> generateResult(
      @Parameter(name = "id", description = "Evaluation identifier for result generation", required = true) @PathVariable("id") Long id) {
    evaluationFacade.generateResult(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete test evaluation",
      description = "Delete a test evaluation from the system",
      operationId = "evaluation:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test evaluation deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Test evaluation not found")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Evaluation identifier for deletion", required = true) @PathVariable("id") Long id) {
    evaluationFacade.delete(id);
  }

  @Operation(summary = "Query evaluation details",
      description = "Retrieve detailed information of a specific test evaluation including configuration and results",
      operationId = "evaluation:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Evaluation details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Test evaluation not found")
  })
  @GetMapping("/{id}")
  public ApiLocaleResult<EvaluationDetailVo> detail(
      @Parameter(name = "id", description = "Evaluation identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(evaluationFacade.detail(id));
  }

  @Operation(summary = "Query evaluation list",
      description = "Retrieve paginated list of test evaluations with filtering and search capabilities",
      operationId = "evaluation:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Evaluation list retrieved successfully")
  })
  @GetMapping
  public ApiLocaleResult<PageResult<EvaluationDetailVo>> list(
      @Valid @ParameterObject EvaluationFindDto dto) {
    return ApiLocaleResult.success(evaluationFacade.list(dto));
  }
}
