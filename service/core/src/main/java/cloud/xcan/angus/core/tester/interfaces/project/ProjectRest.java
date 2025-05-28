package cloud.xcan.angus.core.tester.interfaces.project;


import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.interfaces.project.facade.ProjectFacade;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.ProjectSearchDto;
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

@Tag(name = "Project", description = "Project Management - Work unit for end-to-end project coordination, "
    + "including resource allocation, test planning, task tracking, requirement management, and workflow automation.")
@Validated
@RestController
@RequestMapping("/api/v1/project")
public class ProjectRest {

  @Resource
  private ProjectFacade projectFacade;

  @Operation(summary = "Add the project", operationId = "project:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ProjectAddDto dto) {
    return ApiLocaleResult.success(projectFacade.add(dto));
  }

  @Operation(summary = "Update the project", operationId = "project:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ProjectUpdateDto dto) {
    projectFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Replace the project", operationId = "project:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Replaced successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ProjectReplaceDto dto) {
    return ApiLocaleResult.success(projectFacade.replace(dto));
  }

  @Operation(summary = "Import the inner project example", operationId = "project:example:import")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Imported successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<IdKey<Long, Object>> importExample(
      @Parameter(name = "name", description = "Project name") @RequestParam(value = "name", required = false) String name,
      @Parameter(name = "type", description = "Project type", required = true) @RequestParam("type") ProjectType type,
      @Parameter(name = "dataTypes", description = "Example data types. Import all example data when empty.")
      @RequestParam(value = "dataTypes", required = false) Set<ExampleDataType> dataTypes) {
    return ApiLocaleResult.success(projectFacade.importExample(name, type, dataTypes));
  }

  @Operation(summary = "Delete the project", operationId = "project:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @Parameter(name = "id", description = "Project id", required = true) @PathVariable("id") Long id) {
    projectFacade.delete(id);
  }

  @Operation(summary = "Query the projects that users have joined", operationId = "project:user:joined")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/user/{userId}/joined")
  public ApiLocaleResult<List<ProjectDetailVo>> userJoined(
      @Parameter(name = "userId", description = "User id", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "name", description = "Project name", required = false) @RequestParam(value = "name", required = false) String name) {
    return ApiLocaleResult.success(projectFacade.userJoined(userId, name));
  }

  @Operation(summary = "Query the project detail", operationId = "project:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ProjectDetailVo> detail(
      @PathVariable("id") @Parameter(name = "id", description = "Project id", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.detail(id));
  }

  @Operation(summary = "Query all user members of the project ", operationId = "project:member:user:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/member/user")
  public ApiLocaleResult<List<UserInfo>> userMember(
      @PathVariable("id") @Parameter(name = "id", description = "Project id", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.userMember(id));
  }

  @Operation(summary = "Query the the list of project", operationId = "project:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ProjectDetailVo>> list(
      @Valid @ParameterObject ProjectFindDto dto) {
    return ApiLocaleResult.success(projectFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the list of project", operationId = "project:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ProjectDetailVo>> search(
      @Valid @ParameterObject ProjectSearchDto dto) {
    return ApiLocaleResult.success(projectFacade.search(dto));
  }

}
