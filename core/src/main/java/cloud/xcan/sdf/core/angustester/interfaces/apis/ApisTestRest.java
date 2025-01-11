package cloud.xcan.sdf.core.angustester.interfaces.apis;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestTaskGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ApisTest")
@Validated
@RestController
@RequestMapping("/api/v1/apis")
public class ApisTestRest {

  @Resource
  private ApisTestFacade apisTestFacade;

  @ApiOperation(value =
      "Enable or disable the functionality, performance, stability testing of apis. "
          + "After enabled, the test will be marked as a mandatory activity and the results will be included in the performance analysis.", nickname = "apis:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @ApiParam(value = "Apis test type, allowable values: PERFORMANCE, FUNCTIONAL, STABILITY", allowableValues = "PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    apisTestFacade.testEnabled(apisId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Find enabled functionality, performance, stability testing type of apis.", nickname = "apis:test:enabled:find")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/test/enabled")
  public ApiLocaleResult<List<TestType>> testEnabledFind(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testEnabledFind(apisId));
  }

  @ApiOperation(value = "Configure and generate the functionality, performance, stability testing scripts of apis", nickname = "apis:test:script:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Configure and generate successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/script/generate")
  public ApiLocaleResult<?> scriptGenerate(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @Valid @NotEmpty @RequestBody Set<ApisTestScriptGenerateDto> dtos) {
    apisTestFacade.scriptGenerate(apisId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the functionality, performance, stability testing scripts of apis", nickname = "apis:test:script:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/test/script")
  public void scriptDelete(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    apisTestFacade.scriptDelete(apisId, testTypes);
  }

  @ApiOperation(value = "Configure and generate functionality, performance, stability testing tasks of apis", nickname = "apis:test:task:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Configure and generate successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/test/task/generate")
  public ApiLocaleResult<?> taskGenerate(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @ApiParam(name = "taskSprintId", value = "Task sprint id, it is required for agile project management") @RequestParam(value = "taskSprintId", required = false) Long taskSprintId,
      @Valid @RequestBody Set<ApisTestTaskGenerateDto> dto) {
    apisTestFacade.testTaskGenerate(apisId, taskSprintId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the existing functionality, performance, stability testing tasks of apis", nickname = "apis:test:task:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Restarted Successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/{id}/test/task/restart")
  public ApiLocaleResult<?> taskRetest(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId) {
    apisTestFacade.testTaskRetest(apisId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reopen the existing functionality, performance, stability testing tasks of the apis", nickname = "apis:test:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Reopened successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/{id}/test/task/reopen")
  public ApiLocaleResult<?> testTaskReopen(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId) {
    apisTestFacade.testTaskReopen(apisId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the functionality, performance, stability testing tasks of apis", nickname = "apis:test:task:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/test/task")
  public void testTaskDelete(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    apisTestFacade.testTaskDelete(apisId, testTypes);
  }

  @ApiOperation(value = "Create the functionality, performance or stability testing execution, if the script does not exist, create the script", nickname = "apis:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisId, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Create the functionality, performance or stability testing execution, if the script does not exist, create the script", nickname = "apis:test:exec:add:batch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/exec")
  public ApiLocaleResult<?> testExecAdd(
      @ApiParam(name = "ids", value = "Apis ids", required = true) @RequestParam("id") HashSet<Long> apisIds,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    apisTestFacade.testExecAdd(apisIds, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Create the functionality case testing execution, if the script does not exist, create the script", nickname = "apis:case:test:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/{id}/case/exec")
  public ApiLocaleResult<?> testCaseExecAdd(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId,
      @ApiParam(name = "caseIds", value = "Case ids", required = true) @RequestParam("caseIds") LinkedHashSet<Long> caseIds) {
    apisTestFacade.testCaseExecAdd(apisId, caseIds);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the test results detail of apis", nickname = "apis:test:result:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/test/result/detail")
  public ApiLocaleResult<TestResultDetailVo> testResultDetail(
      @ApiParam(name = "id", value = "Apis id", required = true) @PathVariable("id") Long apisId) {
    return ApiLocaleResult.success(apisTestFacade.testResultDetail(apisId));
  }

}
