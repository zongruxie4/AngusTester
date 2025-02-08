package cloud.xcan.sdf.core.angustester.interfaces.services;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.sdf.api.dto.OrgAndDateFilterDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesSchemaFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.test.ServicesTestTaskGenerateDto;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.services.ApisTestCount;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashSet;
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

@Api(tags = "ServicesTest")
@Validated
@RestController
@RequestMapping("/api/v1")
public class ServicesTestRest {

  @Resource
  private ServicesTestFacade servicesTestFacade;

  @Resource
  private ServicesSchemaFacade servicesSchemaFacade;

  @ApiOperation(value =
      "Enable or disable the functionality, performance, stability testing of service apis. "
          + "After enabled, the test will be marked as a mandatory activity and the results will be included in the performance analysis.", nickname = "services:test:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/test/enabled")
  public ApiLocaleResult<?> testEnabled(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long serviceId,
      @Valid @NotEmpty @ApiParam(value = "Apis test type, allowable values: PERFORMANCE, FUNCTIONAL, STABILITY", allowableValues = "PERFORMANCE, FUNCTIONAL, STABILITY", required = true) @RequestParam(value = "testTypes") HashSet<TestType> testTypes,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    servicesTestFacade.testEnabled(serviceId, testTypes, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of service", nickname = "services:test:apis:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/services/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countServiceTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long serviceId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countServiceTestApis(serviceId, dto));
  }

  @ApiOperation(value = "The testing apis summary the functionality, performance, stability testing of project", nickname = "project:test:apis:count")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/project/{id}/test/apis/count")
  public ApiLocaleResult<ApisTestCount> countProjectTestApis(
      @ApiParam(name = "id", required = true) @PathVariable("id") Long projectId,
      OrgAndDateFilterDto dto) {
    return ApiLocaleResult.success(servicesTestFacade.countProjectTestApis(projectId, dto));
  }

  @ApiOperation(value = "Configure and generate the performance, stability testing scripts of services", nickname = "services:test:script:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Configure and generate successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/test/script/generate")
  public ApiLocaleResult<?> scriptGenerate(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId,
      @Valid @NotEmpty @RequestBody Set<ApisTestScriptGenerateDto> dtos) {
    servicesTestFacade.scriptGenerate(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the performance, stability testing scripts of services", nickname = "services:test:script:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/services/{id}/test/script")
  public void scriptDelete(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    servicesTestFacade.scriptDelete(serviceId, testTypes);
  }

  @ApiOperation(value = "Configure and generate performance, stability testing of services, which will override the configuration when the task exists", nickname = "services:test:task:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Configure and generate successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PutMapping("/services/{id}/test/task/generate")
  public ApiLocaleResult<?> generate(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId,
      @ApiParam(name = "taskSprintId", value = "Task sprint id, it is required for agile project management") @RequestParam(value = "taskSprintId", required = false) Long taskSprintId,
      @Valid @NotEmpty @RequestBody Set<ServicesTestTaskGenerateDto> dto) {
    servicesTestFacade.testTaskGenerate(serviceId, taskSprintId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the existing performance, stability testing of services", nickname = "services:test:task:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Restarted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/services/{id}/test/task/restart")
  public ApiLocaleResult<?> testTaskRestart(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId) {
    servicesTestFacade.testTaskRestart(serviceId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Reopen the existing performance, stability testing of services", nickname = "services:test:task:reopen")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Reopened successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @PatchMapping("/services/{id}/test/task/reopen")
  public ApiLocaleResult<?> testTaskReopen(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId) {
    servicesTestFacade.testTaskReopen(serviceId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the performance, stability testing of services", nickname = "services:test:task:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/services/{id}/test/task")
  public void testTaskDelete(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes) {
    servicesTestFacade.testTaskDelete(serviceId, testTypes);
  }

  @ApiOperation(value = "Query all server configurations of the services", nickname = "services:test:schema:server:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @GetMapping("/services/{id}/test/schema/server")
  public ApiLocaleResult<List<Server>> serverList(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(servicesSchemaFacade.serverList(serviceId, true));
  }

  @ApiOperation(value = "Create the apis functionality, performance or stability testing execution, if the script does not exist, create the script", nickname = "services:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/exec")
  public ApiLocaleResult<?> testExecAdd(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long servicesId,
      @ApiParam(name = "testTypes", value = "Test type", required = true) @RequestParam("testTypes") HashSet<TestType> testTypes,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testExecAdd(servicesId, testTypes, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Create the apis smoke testing execution, if the script does not exist, create the script", nickname = "services:smoke:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/smoke/exec")
  public ApiLocaleResult<?> testSmokeExecAdd(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long servicesId,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testSmokeExecAdd(servicesId, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Create the apis smoke testing execution, if the script does not exist, create the script", nickname = "services:security:test:apis:exec:add")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Created successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping("/services/{id}/security/exec")
  public ApiLocaleResult<?> testSecurityExecAdd(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long servicesId,
      @RequestBody @Nullable List<Server> servers) {
    servicesTestFacade.testSecurityExecAdd(servicesId, servers);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the test results of services", nickname = "services:test:apis:result:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/services/{id}/test/result")
  public ApiLocaleResult<ExecApisResultInfo> testServiceResult(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long serviceId) {
    return ApiLocaleResult.success(servicesTestFacade.testServiceResult(serviceId));
  }

  @ApiOperation(value = "Query the test results of project", nickname = "project:test:apis:result:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/project/{id}/test/result")
  public ApiLocaleResult<ExecApisResultInfo> testProjectResult(
      @ApiParam(name = "id", value = "Project id", required = true) @PathVariable("id") Long projectId) {
    return ApiLocaleResult.success(servicesTestFacade.testProjectResult(projectId));
  }

}
