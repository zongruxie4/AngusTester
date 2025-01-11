package cloud.xcan.sdf.core.angustester.interfaces.services;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.ServicesFacade;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Services")
@Validated
@RestController
@RequestMapping("/api/v1/services")
public class ServicesRest {

  @Resource
  private ServicesFacade serviceFacade;

  @ApiOperation(value = "Add services", nickname = "services:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ServicesAddDto dto) {
    return ApiLocaleResult.success(serviceFacade.add(dto));
  }

  @ApiOperation(value = "Replace the name of services", nickname = "services:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/{id}/name")
  public ApiLocaleResult<?> rename(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "name", value = "New services name", required = true) @Valid @NotBlank @Length(max = DEFAULT_NAME_LENGTH) @RequestParam(value = "name") String name) {
    serviceFacade.rename(id, name);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete services", nickname = "services:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id) {
    serviceFacade.delete(id);
  }

  @ApiOperation(value = "Clone services", nickname = "services:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Cloned clone", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/clone")
  public ApiLocaleResult<?> clone(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id) {
    serviceFacade.clone(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Import the inner project sample", nickname = "services:sample:import")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/{id}/sample/import")
  public ApiLocaleResult<IdKey<Long, Object>> sampleImport(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(serviceFacade.sampleImport(id));
  }

  @ApiOperation(value = "Import the apis to services", nickname = "services:import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> imports(@Valid ServicesImportDto dto) {
    return ApiLocaleResult.success(serviceFacade.imports(dto));
  }

  @DoInFuture("Limit the number of exports")
  @ApiOperation(value = "Export the apis from services", nickname = "services:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid ServicesExportDto dto, HttpServletResponse response) {
    return serviceFacade.export(dto, response);
  }

  @ApiOperation(value = "Modify services status", nickname = "services:status:update",
      notes = "Note: When modifying a project, all services and apis status under the project will be synchronously modified.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Modified successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/status")
  public ApiLocaleResult<?> statusUpdate(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "status", value = "Services status", required = true) @RequestParam("status") ApiStatus status) {
    serviceFacade.statusUpdate(id, status);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the detail of services", nickname = "services:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ServicesDetailVo> detail(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id,
      @ApiParam(name = "joinSchemaFlag", value = "Whether to associate project schema information flag", required = false)
      @RequestParam(name = "joinSchemaFlag", required = false) Boolean joinSchemaFlag) {
    return ApiLocaleResult.success(serviceFacade.detail(id, joinSchemaFlag));
  }

  @ApiOperation(value = "Query the Mock service information associated with a services", nickname = "services:association:mock:service")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/association/mock/service")
  public ApiLocaleResult<MockServiceDetailVo> associationMockService(
      @ApiParam(name = "id", value = "Services id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(serviceFacade.associationMockService(id));
  }

  @ApiOperation(value = "Query the list of services", nickname = "services:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ServiceVo>> list(ServicesFindDto dto) {
    PageResult<ServiceVo> result = serviceFacade.list(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @ApiOperation(value = "Fulltext search the services", nickname = "services:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ServiceVo>> search(ServicesSearchDto dto) {
    PageResult<ServiceVo> result = serviceFacade.search(dto);
    return assembleAllowImportSampleStatus(result);
  }

  @NotNull
  private ApiLocaleResult<PageResult<ServiceVo>> assembleAllowImportSampleStatus(
      PageResult<ServiceVo> result) {
    ApiLocaleResult<PageResult<ServiceVo>> apiResult = ApiLocaleResult.success(result);
    Object queryAll = PrincipalContext.getExtension("queryAllEmpty");
    if (result.isEmpty() && nonNull(queryAll) && (boolean) queryAll) {
      apiResult.getExt().put("allowImportSamples", true);
    }
    return apiResult;
  }

}
