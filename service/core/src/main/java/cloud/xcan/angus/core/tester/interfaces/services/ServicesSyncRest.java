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

@Tag(name = "Services Synchronization", description = "OpenAPI Document Synchronization Management API - Synchronization system for service implementations with Swagger/OpenAPI documentation")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesSyncRest {

  @Resource
  private ServicesSyncFacade servicesSyncFacade;

  @Operation(summary = "Replace service synchronization configuration",
      description =
          "Configure synchronization settings for service with OpenAPI documentation integration. Allow up to "
              + MAX_SYNC_OPENAPI_NUM + " configurations to be added.",
      operationId = "services:sync:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service synchronization configuration replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/synchronization")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "serviceId", description = "Service identifier for synchronization configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody ServicesSyncReplaceDto dto) {
    servicesSyncFacade.replace(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace all service synchronization configurations",
      description =
          "Configure all synchronization settings for service with OpenAPI documentation integration. Allow up to "
              + MAX_SYNC_OPENAPI_NUM + " configurations to be added.",
      operationId = "services:sync:all:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All service synchronization configurations replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PutMapping("/{serviceId}/synchronization/all")
  public ApiLocaleResult<?> replaceAll(
      @Parameter(name = "serviceId", description = "Service identifier for synchronization configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @RequestBody List<ServicesSyncReplaceDto> dto) {
    servicesSyncFacade.replaceAll(serviceId, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Execute service synchronization",
      description = "Execute synchronization process for service using OpenAPI documentation configuration.",
      operationId = "services:sync:exec")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service synchronization executed successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")
  })
  @PostMapping("/{serviceId}/synchronization/exec")
  public ApiLocaleResult<?> sync(
      @Parameter(name = "serviceId", description = "Service identifier for synchronization execution", required = true) @PathVariable Long serviceId,
      @Parameter(name = "name", description = "Synchronization configuration name for selective execution; synchronize all OpenAPI docs under the project when empty") @RequestParam(value = "name", required = false) String name) {
    servicesSyncFacade.sync(serviceId, name);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Test synchronization configuration",
      description = "Validate synchronization URL and authorization configuration for connectivity and access verification.",
      operationId = "services:sync:test")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Synchronization configuration test completed successfully")})
  @PostMapping("/synchronization/test")
  public ApiLocaleResult<?> test(@RequestBody ServicesSyncTestDto dto) {
    servicesSyncFacade.test(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete service synchronization configuration",
      description = "Remove synchronization configuration by names for service management.",
      operationId = "services:sync:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Service synchronization configuration deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/synchronization")
  public void delete(
      @Parameter(name = "serviceId", description = "Service identifier for synchronization configuration", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "names", description = "Synchronization configuration names for deletion", required = true) @RequestParam("names") Set<String> names) {
    servicesSyncFacade.delete(serviceId, names);
  }

  @Operation(summary = "Query service synchronization configuration",
      description = "Retrieve synchronization configuration list for service management and monitoring.",
      operationId = "services:sync:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Service synchronization configuration retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Service not found")})
  @GetMapping(value = "/{serviceId}/synchronization")
  public ApiLocaleResult<List<ServicesSyncDetailVo>> list(
      @Parameter(name = "serviceId", description = "Service identifier for synchronization query", required = true) @PathVariable("serviceId") Long serviceId) {
    return ApiLocaleResult.success(servicesSyncFacade.list(serviceId));
  }

}
