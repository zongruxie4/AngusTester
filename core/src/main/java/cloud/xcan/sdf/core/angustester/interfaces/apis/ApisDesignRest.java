package cloud.xcan.sdf.core.angustester.interfaces.apis;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisDesignFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignContentReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design.ApisDesignUpdateNameDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.design.ApisDesignVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ApisDesign")
@Validated
@RestController
@RequestMapping("/api/v1/apis/design")
public class ApisDesignRest {

  @Resource
  private ApisDesignFacade apisDesignFacade;

  @ApiOperation(value = "Add the design of apis", nickname = "apis:design:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ApisDesignAddDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.add(dto));
  }

  @ApiOperation(value = "Replace the name of apis design", nickname = "apis:design:name:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<?> updateName(@Valid @RequestBody ApisDesignUpdateNameDto dto) {
    apisDesignFacade.updateName(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the design content of apis", nickname = "apis:design:content:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PutMapping("/content")
  public ApiLocaleResult<?> replaceContent(@Valid @RequestBody ApisDesignContentReplaceDto dto) {
    apisDesignFacade.replaceContent(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Release the design of apis", nickname = "apis:design:release")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Release successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PutMapping("/{id}/release")
  public ApiLocaleResult<?> release(
      @ApiParam(name = "id", value = "Design id", required = true) @PathVariable("id") Long id) {
    apisDesignFacade.release(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone the design of apis", nickname = "apis:design:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PutMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Design id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisDesignFacade.clone(id));
  }

  @ApiOperation(value = "Generate the services of designed apis", nickname = "apis:design:services:generate")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PutMapping("/{id}/services/generate")
  public ApiLocaleResult<?> servicesGenerate(
      @ApiParam(name = "id", value = "Design id", required = true) @PathVariable("id") Long id) {
    apisDesignFacade.servicesGenerate(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Import the apis design", nickname = "apis:design:import",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiLocaleResult<IdKey<Long, Object>> imports(@Valid ApisDesignImportDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.imports(dto));
  }

  @ApiOperation(value = "Delete the design of apis", nickname = "apis:design:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Apis design ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisDesignFacade.delete(ids);
  }

  @ApiOperation(value = "Query the sharing detail of apis", nickname = "space:design:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisDesignDetailVo> detail(
      @ApiParam(name = "id", value = "Design id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisDesignFacade.detail(id));
  }

  @ApiOperation(value = "Query the design list of apis", nickname = "apis:design:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisDesignVo>> list(@Valid ApisDesignFindDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the design of apis", nickname = "apis:design:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisDesignVo>> search(@Valid ApisDesignSearchDto dto) {
    return ApiLocaleResult.success(apisDesignFacade.search(dto));
  }

  @ApiOperation(value = "Export the designed OpenAPI specification of apis", nickname = "apis:design:export")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Exported Successfully", response = ApiLocaleResult.class)})
  @GetMapping(value = "/export")
  public ResponseEntity<org.springframework.core.io.Resource> export(
      @Valid ApisDesignExportDto dto, HttpServletResponse response) {
    return apisDesignFacade.export(dto, response);
  }

}
