package cloud.xcan.sdf.core.angustester.interfaces.services;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_CONTENT_LENGTH;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.domain.services.comp.ServicesCompType;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesCompFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.comp.ServicesCompDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
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

@Api(tags = "ServicesComp")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesCompRest {

  @Resource
  private ServicesCompFacade servicesCompFacade;

  @ApiOperation(value = "Replace the OpenAPI component of services", nickname = "services:comp:replace",
      notes = "For more details on the Components Object, please see: [OpenAPI Specification#Components Object](https://swagger.io/specification/#components-object).")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)
  })
  @PutMapping("/{serviceId}/comp/{type}/{key}")
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "type", value = "Component type", required = true) @PathVariable("type") ServicesCompType type,
      @ApiParam(name = "key", value = "Component key", required = true) @PathVariable("key") String key,
      @RequestBody @NotEmpty @Length(max = MAX_PARAM_CONTENT_LENGTH) String component) {
    return ApiLocaleResult.success(servicesCompFacade.replace(serviceId, type, key, component));
  }

  @ApiOperation(value = "Delete services OpenAPI components by type", nickname = "services:comp:deleteByType",
      notes = "If the component keys parameter is empty, all components under the type will be deleted.")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp/{type}")
  public void deleteByType(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "type", value = "Component type", required = true) @PathVariable("type") ServicesCompType type,
      @ApiParam(name = "keys", value = "Component keys", required = false) @RequestParam(required = false) Set<String> keys) {
    servicesCompFacade.deleteByType(serviceId, type, keys);
  }

  @ApiOperation(value = "Delete services OpenAPI components by reference", nickname = "services:comp:deleteByRef")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp")
  public void deleteByRef(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "refs", value = "Component references", required = true) @RequestParam("refs") Set<String> refs) {
    servicesCompFacade.deleteByRef(serviceId, refs);
  }

  @ApiOperation(value = "Delete all OpenAPI components of the services", nickname = "services:comp:all:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{serviceId}/comp/all")
  public void deleteAll(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    servicesCompFacade.deleteAll(serviceId);
  }

  @ApiOperation(value = "Query services OpenAPI component by reference", nickname = "services:comp:detailByRef")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/comp/ref")
  public ApiLocaleResult<ServicesCompDetailVo> detailByRef(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "ref", value = "Component reference", required = true) String ref) {
    return ApiLocaleResult.success(servicesCompFacade.detailByRef(serviceId, ref));
  }

  @ApiOperation(value = "Query components of the specified type in the services", nickname = "services:comp:listByType")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/comp/type")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listByType(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "types", value = "Component types", required = true) @RequestParam(required = true) Set<ServicesCompType> types,
      @ApiParam(name = "keys", value = "Component keys", required = false) @RequestParam(required = false) Set<String> keys,
      @ApiParam(name = "ignoreModel", value = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult
        .success(servicesCompFacade.listByType(serviceId, types, keys, ignoreModel));
  }

  @ApiOperation(value = "Query services components by reference", nickname = "services:comp:listByRef",
      notes = "Note: `If the service type also returns the parent project server configuration`.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/comp")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listByRef(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "refs", value = "Component references", required = true) Set<String> refs,
      @ApiParam(name = "ignoreModel", value = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult
        .success(servicesCompFacade.listByRef(serviceId, refs, ignoreModel));
  }

  @ApiOperation(value = "Query services components by reference", nickname = "services:comp:listAll",
      notes = "Note: `If the service type also returns the parent project server configuration`.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)
  })
  @GetMapping("/{serviceId}/comp/all")
  public ApiLocaleResult<List<ServicesCompDetailVo>> listAll(
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId,
      @ApiParam(name = "ignoreModel", value = "Ignore query model, default false", required = false) @RequestParam(required = false) Boolean ignoreModel) {
    return ApiLocaleResult.success(servicesCompFacade.listAll(serviceId, ignoreModel));
  }
}
