package cloud.xcan.sdf.core.angustester.interfaces.services;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_SYNC_OPENAPI_NUM;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesSyncFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.config.ServicesSyncTestDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ServicesSync")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesSyncRest {

  @Resource
  private ServicesSyncFacade servicesSyncFacade;

  @ApiOperation(value = "Replace synchronization configuration of the services", nickname = "services:sync:replace",
      notes = "Replace synchronization configuration of the services, allow up to "
          + MAX_SYNC_OPENAPI_NUM + " configuration to be added")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/synchronization")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody ServicesSyncReplaceDto dto) {
    servicesSyncFacade.replace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace all synchronization configuration of the services", nickname = "services:sync:all:replace",
      notes = "Replace all synchronization configuration of the services, allow up to "
          + MAX_SYNC_OPENAPI_NUM + " configuration to be added")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/synchronization/all")
  public ApiLocaleResult<?> replaceAll(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<ServicesSyncReplaceDto> dtos) {
    servicesSyncFacade.replaceAll(serviceId, dtos);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Execute the services synchronization", nickname = "services:sync:exec",
      notes = "Execute the synchronization of services by OpenAPI docs configuration")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)
  })
  @PostMapping("/{serviceId}/synchronization/exec")
  public ApiLocaleResult<?> sync(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable Long serviceId,
      @ApiParam(name = "name", value = "Synchronization configuration name, synchronize all OpenAPI docs under the project when the value is empty", required = false) @RequestParam(value = "name", required = false) String name) {
    servicesSyncFacade.sync(serviceId, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Test the synchronization configuration", nickname = "services:sync:test",
      notes = "Test whether the synchronization url and authorization information are configured correctly")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Test successfully", response = ApiLocaleResult.class)})
  @PostMapping("/synchronization/test")
  public ApiLocaleResult<?> test(@RequestBody ServicesSyncTestDto dto) {
    servicesSyncFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the services synchronization configuration", nickname = "services:sync:delete",
      notes = "Delete the OpenAPI docs synchronization configuration of services by name")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Test successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/synchronization")
  public void delete(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "names", value = "Synchronization configuration names", required = true) @RequestParam("names") Set<String> names) {
    servicesSyncFacade.delete(serviceId, names);
  }

  @ApiOperation(value = "Query the services synchronization configuration", nickname = "services:sync:list",
      notes = "Query the all synchronization configuration of services")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Services not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{serviceId}/synchronization")
  public ApiLocaleResult<List<ServicesSyncDetailVo>> list(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSyncFacade.list(serviceId));
  }

}
