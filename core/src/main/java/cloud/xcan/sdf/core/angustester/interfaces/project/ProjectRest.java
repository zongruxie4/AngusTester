package cloud.xcan.sdf.core.angustester.interfaces.project;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.project.ProjectType;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.ProjectFacade;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.ProjectUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.project.facade.vo.ProjectDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.validation.Valid;
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

@Api(tags = "Project")
@Validated
@RestController
@RequestMapping("/api/v1/project")
public class ProjectRest {

  @Resource
  private ProjectFacade projectFacade;

  @ApiOperation(value = "Add the project", nickname = "project:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody ProjectAddDto dto) {
    return ApiLocaleResult.success(projectFacade.add(dto));
  }

  @ApiOperation(value = "Update the project", nickname = "project:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ProjectUpdateDto dto) {
    projectFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace the project", nickname = "project:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(@Valid @RequestBody ProjectReplaceDto dto) {
    return ApiLocaleResult.success(projectFacade.replace(dto));
  }

  @ApiOperation(value = "Import the inner project example", nickname = "project:example:import")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Imported successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PostMapping(value = "/example/import")
  public ApiLocaleResult<IdKey<Long, Object>> importExample(
      @ApiParam(name = "name", value = "Project name") @RequestParam(value ="name", required = false) String name,
      @ApiParam(name = "type", value = "Project type", required = true) @RequestParam("type") ProjectType type,
      @ApiParam(name = "dataTypes", value = "Example data types. Import all example data when empty.")
      @RequestParam(value = "dataTypes", required = false) Set<ExampleDataType> dataTypes) {
    return ApiLocaleResult.success(projectFacade.importExample(name, type, dataTypes));
  }

  @ApiOperation(value = "Delete the project", nickname = "project:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Project id", required = true) @PathVariable("id") Long id) {
    projectFacade.delete(id);
  }

  @ApiOperation(value = "Query the projects that users have joined", nickname = "project:user:joined")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/user/{userId}/joined")
  public ApiLocaleResult<List<ProjectDetailVo>> userJoined(
      @ApiParam(name = "userId", value = "User id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "name", value = "Project name", required = false) @RequestParam(value = "name", required = false) String name) {
    return ApiLocaleResult.success(projectFacade.userJoined(userId, name));
  }

  @ApiOperation(value = "Query the project detail", nickname = "project:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ProjectDetailVo> detail(
      @PathVariable("id") @ApiParam(name = "id", value = "Project id", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.detail(id));
  }

  @ApiOperation(value = "Query all user members of the project ", nickname = "project:member:user:all")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/member/user")
  public ApiLocaleResult<List<UserInfo>> userMember(
      @PathVariable("id") @ApiParam(name = "id", value = "Project id", required = true) Long id) {
    return ApiLocaleResult.success(projectFacade.userMember(id));
  }

  @ApiOperation(value = "Query the the list of project", nickname = "project:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ProjectDetailVo>> list(@Valid ProjectFindDto dto) {
    return ApiLocaleResult.success(projectFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of project", nickname = "project:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ProjectDetailVo>> search(@Valid ProjectSearchDto dto) {
    return ApiLocaleResult.success(projectFacade.search(dto));
  }

}
