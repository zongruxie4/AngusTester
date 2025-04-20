package cloud.xcan.angus.core.tester.interfaces.services;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_SYNC_OPENAPI_NUM;

import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesSyncFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.config.ServicesSyncTestDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.config.ServicesSyncDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
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

@Tag(name = "ServicesSync", description = "OpenAPI Document Sync - Synchronize service implementations with Swagger/OpenAPI documentation.")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesSyncRest {

  @Resource
  private ServicesSyncFacade servicesSyncFacade;

  @Operation(description =
      "Replace synchronization configuration of the services. Replace synchronization configuration of the services, "
          + "allow up to " + MAX_SYNC_OPENAPI_NUM
          + " configuration to be added", operationId = "services:sync:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @PutMapping("/{serviceId}/synchronization")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody ServicesSyncReplaceDto dto) {
    servicesSyncFacade.replace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(description =
      "Replace all synchronization configuration of the services. Replace all synchronization configuration of the services, allow up to "
          + MAX_SYNC_OPENAPI_NUM
          + " configuration to be added", operationId = "services:sync:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @PutMapping("/{serviceId}/synchronization/all")
  public ApiLocaleResult<?> replaceAll(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<ServicesSyncReplaceDto> dto) {
    servicesSyncFacade.replaceAll(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Execute the services synchronization. Execute the synchronization of services by OpenAPI docs configuration", operationId = "services:sync:exec")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")
  })
  @PostMapping("/{serviceId}/synchronization/exec")
  public ApiLocaleResult<?> sync(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable Long serviceId,
      @Parameter(name = "name", description = "Synchronization configuration name, synchronize all OpenAPI docs under the project when the value is empty", required = false) @RequestParam(value = "name", required = false) String name) {
    servicesSyncFacade.sync(serviceId, name);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Test the synchronization configuration. Test whether the synchronization url and authorization information are configured correctly", operationId = "services:sync:test")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Test successfully")})
  @PostMapping("/synchronization/test")
  public ApiLocaleResult<?> test(@RequestBody ServicesSyncTestDto dto) {
    servicesSyncFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Delete the services synchronization configuration. Delete the OpenAPI docs synchronization configuration of services by name", operationId = "services:sync:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Test successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/synchronization")
  public void delete(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "names", description = "Synchronization configuration names", required = true) @RequestParam("names") Set<String> names) {
    servicesSyncFacade.delete(serviceId, names);
  }

  @Operation(description = "Query the services synchronization configuration. Query the all synchronization configuration of services", operationId = "services:sync:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Services not found")})
  @GetMapping(value = "/{serviceId}/synchronization")
  public ApiLocaleResult<List<ServicesSyncDetailVo>> list(
      @Parameter(name = "serviceId", description = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSyncFacade.list(serviceId));
  }

}
