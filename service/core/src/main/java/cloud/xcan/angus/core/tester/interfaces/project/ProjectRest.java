package cloud.xcan.angus.core.tester.interfaces.project;


import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.interfaces.project.facade.ProjectFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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

@Tag(name = "Project", description = "Project Management API - Project lifecycle management including creation, configuration, member management, and workflow coordination for testing and development activities")
@Validated
@RestController
@RequestMapping("/api/v1/project")
public class ProjectRest {

  @Resource
  private ProjectFacade projectFacade;

  @Operation(summary = "Create new project",
      description = "Create a new project with configuration including members, timeline, and methodology setup",
      operationId = "project:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Project created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ProjectAddDto dto) {
    return ApiLocaleResult.success(projectFacade.add(dto));
  }

  @Operation(summary = "Update project configuration",
      description = "Modify existing project properties including name, timeline, members, and description",
      operationId = "project:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ProjectUpdateDto dto) {
    projectFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace project configuration",
      description = "Replace project with new configuration or create new project if identifier is null",
      operationId = "project:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project replaced successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ProjectReplaceDto dto) {
    return ApiLocaleResult.success(projectFacade.replace(dto));
  }

  @Operation(summary = "Import project example data",
      description = "Import predefined example data for rapid project setup and demonstration purposes",
      operationId = "project:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Project example imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<IdKey<Long, Object>> importExample(
      @Parameter(name = "name", description = "Project name for example import") @RequestParam(value = "name", required = false) String name,
      @Parameter(name = "type", description = "Project type for methodology setup", required = true) @RequestParam("type") ProjectType type,
      @Parameter(name = "dataTypes", description = "Example data types to import; imports all when empty")
      @RequestParam(value = "dataTypes", required = false) Set<ExampleDataType> dataTypes) {
    return ApiLocaleResult.success(projectFacade.importExample(name, type, dataTypes));
  }

  @Operation(summary = "Delete project",
      description = "Permanently delete project and move to recycle bin for potential recovery",
      operationId = "project:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Project deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Project identifier for deletion", required = true) @PathVariable("id") Long id) {
    projectFacade.delete(id);
  }

  @Operation(summary = "Query user's joined projects",
      description = "Retrieve all projects that a specific user has joined or has access to",
      operationId = "project:user:joined")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User's joined projects retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "User not found")})
  @GetMapping(value = "/user/{userId}/joined")
  public ApiLocaleResult<List<ProjectDetailVo>> userJoined(
      @Parameter(name = "userId", description = "User identifier for project membership query", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "name", description = "Project name for filtering joined projects") @RequestParam(value = "name", required = false) String name) {
    return ApiLocaleResult.success(projectFacade.userJoined(userId, name));
  }

  @Operation(summary = "Query project details",
      description = "Retrieve project information including configuration, members, and status",
      operationId = "project:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ProjectDetailVo> detail(
      @PathVariable("id") @Parameter(name = "id", description = "Project identifier for detail query", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.detail(id));
  }

  @Operation(summary = "Query all project user members",
      description = "Retrieve complete list of user members associated with the project",
      operationId = "project:member:user:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project user members retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Project not found")})
  @GetMapping(value = "/{id}/member/user")
  public ApiLocaleResult<List<UserInfo>> userMember(
      @PathVariable("id") @Parameter(name = "id", description = "Project identifier for member query", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.userMember(id));
  }

  @Operation(summary = "Query project list",
      description = "Retrieve paginated list of projects with filtering and search capabilities",
      operationId = "project:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Project list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ProjectDetailVo>> list(
      @Valid @ParameterObject ProjectFindDto dto) {
    return ApiLocaleResult.success(projectFacade.list(dto));
  }

}
