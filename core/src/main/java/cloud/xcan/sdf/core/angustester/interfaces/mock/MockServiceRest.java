package cloud.xcan.sdf.core.angustester.interfaces.mock;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockServiceFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceFileImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceServicesAssocDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceListVo;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
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

@Api(tags = "MockService")
@Validated
@RestController
@RequestMapping("/api/v1/mock/service")
public class MockServiceRest {

  @Resource
  private MockServiceFacade mockServiceFacade;

  @ApiOperation(value = "Add mock service", nickname = "mock:service:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody MockServiceAddDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.add(dto));
  }

  @ApiOperation(value = "Update mock service", nickname = "mock:service:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody MockServiceUpdateDto dto) {
    mockServiceFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace mock service", nickname = "mock:service:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody MockServiceReplaceDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.replace(dto));
  }

  @ApiOperation(value = "Sync the service setting and apis to mock service instance", nickname = "mock:service:instance:sync")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Synchronized successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @ResponseStatus(HttpStatus.OK)
  @PutMapping(value = "/{id}/instance/sync")
  public ApiLocaleResult<?> instanceSync(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.instanceSync(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Import the mock service from file", nickname = "mock:service:file:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/file/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
  public ApiLocaleResult<IdKey<Long, Object>> fileImport(@Valid MockServiceFileImportDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.fileImport(dto));
  }

  @ApiOperation(value = "Generate associated mock service based on the services", nickname = "mock:service:services:association:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Associated successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/association/services")
  public ApiLocaleResult<IdKey<Long, Object>> servicesAssoc(
      @Valid @RequestBody MockServiceServicesAssocDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.servicesAssoc(dto));
  }

  @ApiOperation(value = "Associated mock service based on the services", nickname = "mock:service:services:association:update")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Associated successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PutMapping(value = "/association/{mockServiceId}/{serviceId}")
  public ApiLocaleResult<?> servicesAssocUpdate(
      @ApiParam(name = "mockServiceId", value = "Mock service id", required = true) @PathVariable("mockServiceId") Long mockServiceId,
      @ApiParam(name = "serviceId", value = "Services id", required = true) @PathVariable("serviceId") Long serviceId) {
    mockServiceFacade.servicesAssocUpdate(mockServiceId, serviceId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Import the inner mock apis sample", nickname = "mock:service:apis:sample:import")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/{id}/sample/apis/import")
  public ApiLocaleResult<?> sampleApisImport(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.exampleImport(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Import the apis to existed mock service", nickname = "mock:service:apis:import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<?> imports(@Valid MockServiceImportDto dto) {
    mockServiceFacade.imports(dto);
    return ApiLocaleResult.success();
  }

  @DoInFuture("Limit the number of exports")
  @ApiOperation(value = "Export the apis from existed mock service", nickname = "mock:service:apis:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid MockServiceExportDto dto, HttpServletResponse response) {
    return mockServiceFacade.export(dto, response);
  }

  @ApiOperation(value = "Start mock service by agent", nickname = "mock:service:start")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully processed", response = ApiLocaleResult.class)
  })
  @PostMapping("/start")
  public ApiLocaleResult<List<StartVo>> start(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.start(ids));
  }

  @ApiOperation(value = "Stop mock service by agent", nickname = "mock:service:stop")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Successfully processed", response = ApiLocaleResult.class)
  })
  @PostMapping("/stop")
  public ApiLocaleResult<List<StopVo>> stop(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody HashSet<Long> ids) {
    return ApiLocaleResult.success(mockServiceFacade.stop(ids));
  }

  @ApiOperation(value = "Delete the association between mock service and project", nickname = "mock:service:association:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}/association")
  public void assocDelete(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    mockServiceFacade.assocDelete(id);
  }

  @ApiOperation(value = "Delete mock services", nickname = "mock:service:delete")
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Mock service ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids,
      @ApiParam(name = "force", value = "Force deletion flag", required = false)
      @Valid @RequestParam(value = "force", required = false) Boolean force) {
    mockServiceFacade.delete(ids, force);
  }

  @ApiOperation(value = "Query the associated apis ids of mock service", nickname = "mock:service:association:apis:id:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/association/apis/id")
  public ApiLocaleResult<Set<Long>> assocApisIdsList(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.assocApisIdsList(id));
  }

  @ApiOperation(value = "Query the detail of mock service", nickname = "mock:service:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<MockServiceDetailVo> detail(
      @ApiParam(name = "id", value = "Mock service id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(mockServiceFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of mock service", nickname = "mock:service:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<MockServiceListVo>> list(@Valid MockServiceFindDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of mock service", nickname = "mock:service:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<MockServiceListVo>> search(@Valid MockServiceSearchDto dto) {
    return ApiLocaleResult.success(mockServiceFacade.search(dto));
  }
}
