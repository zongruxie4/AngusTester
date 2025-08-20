package cloud.xcan.angus.core.tester.interfaces.module;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.module.facade.ModuleFacade;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleAddDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleFindDto;
import cloud.xcan.angus.core.tester.interfaces.module.facade.dto.ModuleReplaceDto;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Module", description = "Software Module Management - APIs for modular system architecture, enabling creation, organization, and management of independent functional units within a project")
@Validated
@RestController
@RequestMapping("/api/v1/module")
public class ModuleRest {

  @Resource
  private ModuleFacade moduleFacade;

  @Operation(summary = "Create new software modules for a project",
      description = "Add one or more software modules to a project for modular architecture design and management.",
      operationId = "module:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Modules created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> add(@Valid @RequestBody ModuleAddDto dto) {
    return ApiLocaleResult.success(moduleFacade.add(dto));
  }

  @Operation(summary = "Update software modules of a project",
      description = "Batch update properties of existing software modules, such as name, parent, or sequence.",
      operationId = "module:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modules updated successfully")})
  @PatchMapping
  public ApiLocaleResult<?> update(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ModuleUpdateDto> dto) {
    moduleFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace software modules of a project",
      description = "Batch replace software modules with new definitions, supporting full structure updates.",
      operationId = "module:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Modules replaced successfully")})
  @PutMapping
  public ApiLocaleResult<List<IdKey<Long, Object>>> replace(
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestBody List<ModuleReplaceDto> dto) {
    return ApiLocaleResult.success(moduleFacade.replace(dto));
  }

  @Operation(summary = "Import module example structure",
      description = "Import a predefined example module structure into the project for rapid modularization.",
      operationId = "module:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Module example imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<List<IdKey<Long, Object>>> importExample(
      @Parameter(name = "projectId", description = "Project identifier for module import", required = true) @RequestParam("projectId") Long projectId) {
    return ApiLocaleResult.success(moduleFacade.importExample(projectId));
  }

  @Operation(summary = "Delete software modules from a project",
      description = "Batch delete software modules by their identifiers.",
      operationId = "module:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Modules deleted successfully")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Module identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    moduleFacade.delete(ids);
  }

  @Operation(summary = "Query software module details",
      description = "Retrieve detailed information of a specific software module by its identifier.",
      operationId = "module:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Module details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Module not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ModuleVo> detail(
      @Parameter(name = "id", description = "Module identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(moduleFacade.detail(id));
  }

  @Operation(summary = "Query software module list of a project",
      description = "Retrieve a flat list of software modules for a project with filtering and pagination support.",
      operationId = "module:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Module list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<List<ModuleVo>> list(@Valid @ParameterObject ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.list(dto));
  }

  @Operation(summary = "Query software module tree of a project",
      description = "Retrieve a hierarchical tree structure of software modules for a project.",
      operationId = "module:tree")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Module tree retrieved successfully")})
  @GetMapping("/tree")
  public ApiLocaleResult<List<ModuleTreeVo>> tree(@Valid @ParameterObject ModuleFindDto dto) {
    return ApiLocaleResult.success(moduleFacade.tree(dto));
  }

}
