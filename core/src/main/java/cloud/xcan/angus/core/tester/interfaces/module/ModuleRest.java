package cloud.xcan.angus.core.tester.interfaces.module;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.module.facade.ModuleFacade;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleSearchDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.vo.ModuleTreeVo;
import cloud.xcan.angus.core.tester.interfaces.module.facade.vo.ModuleVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
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

@Tag(name = "Module", description = "Software Module Management - APIs for modular system architecture design, enabling creation/organization of independent functional units.")
@Validated
@RestController
@RequestMapping("/api/v1/module")
public class ModuleRest {

  @Resource
  private ModuleFacade moduleFacade;

  @Operation(description = "Add the module of software project", operationId = "module:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody ModuleAddDto dto) {
    return ApiLocaleResult.success(moduleFacade.add(dto));
  }

  @Operation(description = "Update the module of software project", operationId = "module:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ModuleUpdateDto> dto) {
    moduleFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(description = "Replace the module of software project", operationId = "module:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ModuleReplaceDto> dto) {
    return ApiLocaleResult.success(moduleFacade.replace(dto));
  }

  @Operation(description = "Import the inner module example", operationId = "module:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project id", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(moduleFacade.importExample(projectId));
  }

  @Operation(description = "Delete the modules of software project", operationId = "module:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Case module ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    moduleFacade.delete(ids);
  }

  @Operation(description = "Query the module detail of software project", operationId = "module:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ModuleVo> detail(
      @Parameter(name = "id", description = "Module id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(moduleFacade.detail(id));
  }

  @Operation(description = "Query the module list of software project", operationId = "module:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<List<ModuleVo>> list(@Valid ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.list(dto));
  }

  @Operation(description = "Fulltext search the module of software project", operationId = "module:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<List<ModuleVo>> search(@Valid ModuleSearchDto dto) {
    return ApiLocaleResult.success(moduleFacade.search(dto));
  }

  @Operation(description = "Query the module tree of software project", operationId = "module:tree")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/tree")
  public ApiLocaleResult<List<ModuleTreeVo>> tree(@Valid ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.tree(dto));
  }

  @Operation(description = "Fulltext search the module tree of software project", operationId = "module:tree:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/tree/search")
  public ApiLocaleResult<List<ModuleTreeVo>> treeSearch(@Valid ModuleSearchDto dto) {
    return ApiLocaleResult.success(moduleFacade.treeSearch(dto));
  }
}
