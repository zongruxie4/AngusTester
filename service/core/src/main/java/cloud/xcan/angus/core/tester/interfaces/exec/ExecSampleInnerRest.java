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

@Tag(name = "Execution Sample - Internal", description = "Internal Execution Sample Management - Service internal detailed execution sampling data access with performance metrics and analysis")
@Validated
@RestController
@RequestMapping("/innerapi/v1/exec")
public class ExecSampleInnerRest {

  @Resource
  private ExecSampleFacade execSampleFacade;

  @Operation(summary = "Get total summary of last sampling execution",
      description = "Retrieve comprehensive total summary information of the last sampling execution for internal service integration",
      operationId = "exec:sample:summary:total:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Total summary of last sampling execution retrieved successfully")})
  @GetMapping(value = "/{id}/sample/summary/total")
  public ApiLocaleResult<ExecSampleSummaryVo> summaryTotal(
      @Parameter(name = "id", description = "Execution identifier for summary retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execSampleFacade.summaryTotal(id));
  }

  @Operation(summary = "Get execution sampling summary list",
      description = "Retrieve paginated execution sampling summary list with comprehensive performance metrics including duration, errors, iterations, operations, transactions, throughput, and response time statistics",
      operationId = "exec:sample:summary:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling summary list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/summary/list")
  public ApiLocaleResult<PageResult<ExecSampleVo>> summaryList(
      @Parameter(name = "id", description = "Execution identifier for summary list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.summaryList(id, dto));
  }

  @Operation(summary = "Get execution sampling response time list",
      description = "Retrieve paginated execution sampling response time (RT) list for performance analysis",
      operationId = "exec:sample:score:rt:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling response time list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/score")
  public ApiLocaleResult<PageResult<ExecSampleVo>> scoreList(
      @Parameter(name = "id", description = "Execution identifier for response time list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.scoreList(id, dto));
  }

  @Operation(summary = "Get execution sampling throughput list",
      description = "Retrieve paginated execution sampling throughput list for performance analysis",
      operationId = "exec:sample:score:throughput:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling throughput list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/throughput")
  public ApiLocaleResult<PageResult<ExecSampleVo>> throughputList(
      @Parameter(name = "id", description = "Execution identifier for throughput list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.throughputList(id, dto));
  }

  @Operation(summary = "Get execution sampling thread list",
      description = "Retrieve paginated execution sampling thread list for resource utilization analysis",
      operationId = "exec:sample:thread:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling thread list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/thread")
  public ApiLocaleResult<PageResult<ExecSampleVo>> threadList(
      @Parameter(name = "id", description = "Execution identifier for thread list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.threadList(id, dto));
  }

  @Operation(summary = "Get execution sampling error list",
      description = "Retrieve paginated execution sampling error list for error analysis and debugging",
      operationId = "exec:sample:error:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling error list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/error")
  public ApiLocaleResult<PageResult<ExecSampleVo>> errorList(
      @Parameter(name = "id", description = "Execution identifier for error list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.errorList(id, dto));
  }

  @Operation(summary = "Get execution sampling error content list",
      description = "Retrieve paginated execution sampling error content list for detailed error analysis",
      operationId = "exec:sample:error:content:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling error content list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/error/content")
  public ApiLocaleResult<PageResult<ExecSampleErrorContentVo>> errorContent(
      @Parameter(name = "id", description = "Execution identifier for error content list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleErrorContentFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.errorContent(id, dto));
  }

  @Operation(summary = "Get latest execution sampling error counters",
      description = "Retrieve latest execution sampling error counters for real-time error monitoring",
      operationId = "exec:sample:errors:counter:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest execution sampling error counters retrieved successfully")})
  @GetMapping(value = "/{id}/sample/errors/counter/latest")
  public ApiLocaleResult<LinkedHashMap<String, LinkedHashMap<String, Long>>> latestErrorsCounter(
      @Parameter(name = "id", description = "Execution identifier for error counter retrieval", required = true) @PathVariable("id") Long id,
      @Parameter(name = "nodeId", description = "Node identifier for specific node error counter filtering") Long nodeId,
      @Parameter(name = "name", description = "Execution task name for specific task error counter filtering") String name) {
    return ApiLocaleResult.success(execSampleFacade.latestErrorsCounter(id, nodeId, name));
  }

  @Operation(summary = "Get latest execution sampling upload result progress",
      description = "Retrieve latest execution sampling upload result progress for progress monitoring",
      operationId = "exec:sample:uploadResultProgress:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest execution sampling upload result progress retrieved successfully")})
  @GetMapping(value = "/{id}/sample/uploadResultProgress/latest")
  public ApiLocaleResult<ExecSampleUploadResultProgressVo> latestUploadResultProgress(
      @Parameter(name = "id", description = "Execution identifier for upload progress retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(execSampleFacade.latestUploadResultProgress(id));
  }

  @Operation(summary = "Get latest execution sampling extended counter map",
      description = "Retrieve latest execution sampling extended counter map for custom metric analysis",
      operationId = "exec:sample:counter:map:latest:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Latest execution sampling extended counter map retrieved successfully")})
  @GetMapping(value = "/{id}/sample/extension/counter/map1/latest")
  public ApiLocaleResult<LinkedHashMap<String, LinkedHashMap<String, Long>>> latestExtCounterMap1(
      @Parameter(name = "id", description = "Execution identifier for extended counter map retrieval", required = true) @PathVariable("id") Long id,
      @Parameter(name = "nodeId", description = "Node identifier for specific node extended counter filtering") Long nodeId,
      @Parameter(name = "name", description = "Execution task name for specific task extended counter filtering") String name) {
    return ApiLocaleResult.success(execSampleFacade.latestExtCounterMap1(id, nodeId, name));
  }

  @Operation(summary = "Get execution sampling extended content list",
      description = "Retrieve paginated execution sampling extended content list for custom data analysis",
      operationId = "exec:sample:content:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Execution sampling extended content list retrieved successfully")})
  @GetMapping(value = "/{id}/sample/extension/content")
  public ApiLocaleResult<PageResult<ExecSampleContent>> extContentList(
      @Parameter(name = "id", description = "Execution identifier for extended content list retrieval", required = true) @PathVariable("id") Long id,
      @Valid @ParameterObject ExecSampleExtcFindDto dto) {
    return ApiLocaleResult.success(execSampleFacade.extContentList(id, dto));
  }

}
