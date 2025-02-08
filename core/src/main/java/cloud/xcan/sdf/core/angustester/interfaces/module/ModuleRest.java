package cloud.xcan.sdf.core.angustester.interfaces.module;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.ModuleFacade;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto.ModuleUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleTreeVo;
import cloud.xcan.sdf.core.angustester.interfaces.module.facade.vo.ModuleVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
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

@Api(tags = "Module")
@Validated
@RestController
@RequestMapping("/api/v1/module")
public class ModuleRest {

  @Resource
  private ModuleFacade moduleFacade;

  @ApiOperation(value = "Add the module", nickname = "module:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody ModuleAddDto dto) {
    return ApiLocaleResult.success(moduleFacade.add(dto));
  }

  @ApiOperation(value = "Update the module", nickname = "module:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ModuleUpdateDto> dto) {
    moduleFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the module", nickname = "module:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestBody List<ModuleReplaceDto> dto) {
    return ApiLocaleResult.success(moduleFacade.replace(dto));
  }

  @ApiOperation(value = "Import the inner module example", nickname = "module:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @ApiParam(name = "projectId", value = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(moduleFacade.importExample(projectId));
  }

  @ApiOperation(value = "Delete the modules", nickname = "module:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(code = 204, message = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Case module ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    moduleFacade.delete(ids);
  }

  @ApiOperation(value = "Query the detail of module", nickname = "module:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ModuleVo> detail(
      @ApiParam(name = "id", value = "Module id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(moduleFacade.detail(id));
  }

  @ApiOperation(value = "Query the module list", nickname = "module:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<List<ModuleVo>> list(@Valid ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the module", nickname = "module:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<List<ModuleVo>> search(@Valid ModuleSearchDto dto) {
    return ApiLocaleResult.success(moduleFacade.search(dto));
  }

  @ApiOperation(value = "Query the tree of module", nickname = "module:tree")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tree")
  public ApiLocaleResult<List<ModuleTreeVo>> tree(@Valid ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.tree(dto));
  }

  @ApiOperation(value = "Fulltext search the tree of module", nickname = "module:tree:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/tree/search")
  public ApiLocaleResult<List<ModuleTreeVo>> treeSearch(@Valid ModuleSearchDto dto) {
    return ApiLocaleResult.success(moduleFacade.treeSearch(dto));
  }
}
