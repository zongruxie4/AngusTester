package cloud.xcan.angus.core.tester.interfaces.services;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_CONTENT_LENGTH;

import cloud.xcan.angus.core.tester.domain.services.comp.ServicesCompType;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesCompFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.comp.ServicesCompDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Set;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ServicesComponent", description = "Service OpenAPI Component Management - "
    + "Centralized management of OpenAPI components (schemas, responses, security schemes) across services")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesCompRest {

  @Resource
  private ServicesCompFacade servicesCompFacade;

  @Operation(summary = "Replace the service OpenAPI components",
      description =
          "Replace the OpenAPI component of service. For more details on the Components Object, "
              + "please see: [OpenAPI Specification#Components Object](https://swagger.io/specification/#components-object)",
      operationId = "services:comp:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")
  })
  @PutMapping("/{serviceId}/comp/{type}/{key}")
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "type", description = "Component type", required = true) @PathVariable("type") ServicesCompType type,
      @Parameter(name = "key", description = "Component key", required = true) @PathVariable("key") String key,
      @RequestBody @NotEmpty @Length(max = MAX_PARAM_CONTENT_LENGTH) String component) {
    return ApiLocaleResult.success(servicesCompFacade.replace(serviceId, type, key, component));
  }

  @Operation(summary = "Delete the service OpenAPI components by type",
      description = "Delete the OpenAPI components by type of service. If the component keys parameter is empty, "
          + "all components under the type will be deleted",
      operationId = "services:comp:deleteByType")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp/{type}")
  public void deleteByType(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "type", description = "Component type", required = true) @PathVariable("type") ServicesCompType type,
      @Parameter(name = "keys", description = "Component keys", required = false) @RequestParam(required = false) Set<String> keys) {
    servicesCompFacade.deleteByType(serviceId, type, keys);
  }

  @Operation(summary = "Delete the service OpenAPI components by reference", operationId = "services:comp:deleteByRef")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp")
  public void deleteByRef(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "refs", description = "Component references", required = true) @RequestParam("refs") Set<String> refs) {
    servicesCompFacade.deleteByRef(serviceId, refs);
  }

  @Operation(summary = "Delete all OpenAPI components of the service", operationId = "services:comp:all:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp/all")
  public void deleteAll(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId) {
    servicesCompFacade.deleteAll(serviceId);
  }

  @Operation(summary = "Query the service OpenAPI component by reference", operationId = "services:comp:detailByRef")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  })
  @GetMapping("/{serviceId}/comp/ref")
  public ApiLocaleResult<ServicesCompDetailVo> detailByRef(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "ref", description = "Component reference", required = true) String ref) {
    return ApiLocaleResult.success(servicesCompFacade.detailByRef(serviceId, ref));
  }

  @Operation(summary = "Query the service OpenAPI components of the specified type", operationId = "services:comp:listByType")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  })
  @GetMapping("/{serviceId}/comp/type")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listByType(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "types", description = "Component types", required = true) @RequestParam(required = true) Set<ServicesCompType> types,
      @Parameter(name = "keys", description = "Component keys", required = false) @RequestParam(required = false) Set<String> keys,
      @Parameter(name = "ignoreModel", description = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult.success(
        servicesCompFacade.listByType(serviceId, types, keys, ignoreModel));
  }

  @Operation(summary = "Query the service OpenAPI components by reference", operationId = "services:comp:listByRef")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  })
  @GetMapping("/{serviceId}/comp")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listByRef(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "refs", description = "Component references", required = true) Set<String> refs,
      @Parameter(name = "ignoreModel", description = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult.success(servicesCompFacade.listByRef(serviceId, refs, ignoreModel));
  }

  @Operation(summary = "Query the service OpenAPI components by reference", operationId = "services:comp:listAll")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")
  })
  @GetMapping("/{serviceId}/comp/all")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listAll(
      @Parameter(name = "serviceId", description = "Service id", required = true) @PathVariable("serviceId") Long serviceId,
      @Parameter(name = "ignoreModel", description = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult.success(servicesCompFacade.listAll(serviceId, ignoreModel));
  }
}
