package cloud.xcan.angus.core.tester.interfaces.exec;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecSampleContent;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecSampleFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleErrorContentFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleExtcFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample.ExecSampleFindDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.ExecSampleErrorContentVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleUploadResultProgressVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ExecSampleInner", description = "Internal Execution Sampling API - Service internal detailed execution sampling data access")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec")
public class ExecSampleInnerRest {

  @Resource
  private ExecSampleFacade execSampleFacade;

  @Operation(description = "Query the total summary information of the last sampling execution.", operationId = "exec:sample:summary:total:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/summary/total")
  public ApiLocaleResult<ExecSampleSummaryVo> summaryTotal(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execSampleFacade.summaryTotal(id));
  }

  @Operation(description = "Query the summary list of execution sampling. "
      + "Values format: duration,errors,iterations,n,operations,transactions,readBytes,writeBytes,ops,tps,brps,bwps,tranMean,tranMin,tranMax,tranP50,tranP75,tranP90,tranP95,tranP99,tranP999,errorRate,threadPoolSize,threadPoolActiveSize,threadMaxPoolSize,extCounter1,extCounter2,extCounter3,extGauge1,extGauge2,extGauge3",
      operationId = "exec:sample:summary:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/summary/list")
  public ApiLocaleResult<PageResult<ExecSampleVo>> summaryList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.summaryList(id, dto));
  }

  @Operation(description = "Query the score (RT) list of execution sampling.", operationId = "exec:sample:score:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/score")
  public ApiLocaleResult<PageResult<ExecSampleVo>> scoreList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.scoreList(id, dto));
  }

  @Operation(description = "Query the throughput list of execution sampling.", operationId = "exec:sample:score:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/throughput")
  public ApiLocaleResult<PageResult<ExecSampleVo>> throughputList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.throughputList(id, dto));
  }

  @Operation(description = "Query the thread list of execution sampling", operationId = "exec:sample:thread:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/thread")
  public ApiLocaleResult<PageResult<ExecSampleVo>> threadList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.threadList(id, dto));
  }

  @Operation(description = "Query the errors list of execution sampling", operationId = "exec:sample:error:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/error")
  public ApiLocaleResult<PageResult<ExecSampleVo>> errorList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.errorList(id, dto));
  }

  @Operation(description = "Query the error content list of execution sampling", operationId = "exec:sample:error:content:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/error/content")
  public ApiLocaleResult<PageResult<ExecSampleErrorContentVo>> errorContent(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleErrorContentFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.errorContent(id, dto));
  }

  @Operation(description = "Query the latest errors counter of execution sampling", operationId = "exec:sample:errors:counter:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/errors/counter/latest")
  public ApiLocaleResult<LinkedHashMap<String, LinkedHashMap<String, Long>>> latestErrorsCounter(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "nodeId", description = "Node id") Long nodeId,
      @Parameter(name = "name", description = "Execution task name") String name) {
    return ApiLocaleResult.success(execSampleFacade.latestErrorsCounter(id, nodeId, name));
  }

  @Operation(description = "Query the latest upload result progress of execution sampling", operationId = "exec:sample:uploadResultProgress:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/uploadResultProgress/latest")
  public ApiLocaleResult<ExecSampleUploadResultProgressVo> latestUploadResultProgress(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execSampleFacade.latestUploadResultProgress(id));
  }

  @Operation(description = "Query the latest extended counter of execution sampling", operationId = "exec:sample:counter:map:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/extension/counter/map1/latest")
  public ApiLocaleResult<LinkedHashMap<String, LinkedHashMap<String, Long>>> latestExtCounterMap1(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Parameter(name = "nodeId", description = "Node id") Long nodeId,
      @Parameter(name = "name", description = "Execution task name") String name) {
    return ApiLocaleResult.success(execSampleFacade.latestExtCounterMap1(id, nodeId, name));
  }

  @Operation(description = "Query the extended content list of execution sampling", operationId = "exec:sample:content:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping(value = "/{id}/sample/extension/content")
  public ApiLocaleResult<PageResult<ExecSampleContent>> extContentList(
      @Parameter(name = "id", description = "Execution id", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleExtcFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.extContentList(id, dto));
  }

}
