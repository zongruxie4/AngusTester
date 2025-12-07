package cloud.xcan.angus.core.tester.interfaces.scenario;


import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioTrashFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.trash.ScenarioTrashListDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.trash.ScenarioTrashDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Scenario Recycle Bin", description = "Scenario Recycle Bin Management API - Temporary storage and recovery system for deleted scenarios with restore capabilities and permanent deletion controls")
@Validated
@RestController
@RequestMapping("/api/v1/scenario/trash")
public class ScenarioTrashRest {

  @Resource
  private ScenarioTrashFacade trashScenarioFacade;

  @Operation(summary = "Permanently delete scenario from trash",
      description = "Permanently remove a specific scenario from the recycle bin with no recovery option",
      operationId = "scenario:trash:clear")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Scenario permanently deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void clear(
      @Parameter(name = "id", description = "Trash record identifier for permanent deletion", required = true) @PathVariable("id") Long id) {
    trashScenarioFacade.clear(id);
  }

  @Operation(summary = "Clear all scenarios from trash",
      description = "Permanently remove all scenarios from the recycle bin for the specified project",
      operationId = "scenario:trash:clear:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "All scenarios permanently deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping
  public void clearAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for bulk deletion") Long projectId) {
    trashScenarioFacade.clearAll(projectId);
  }

  @Operation(summary = "Restore scenario from trash",
      description = "Recover a specific scenario from the recycle bin and restore it to active status",
      operationId = "scenario:trash:back")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/back")
  public ApiLocaleResult<?> back(
      @Parameter(name = "id", description = "Trash record identifier for scenario restoration", required = true) @PathVariable("id") Long id) {
    trashScenarioFacade.back(id);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Restore all scenarios from trash",
      description = "Recover all scenarios from the recycle bin for the specified project",
      operationId = "scenario:trash:back:all")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "All scenarios restored successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/back")
  public ApiLocaleResult<?> backAll(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for bulk restoration") Long projectId) {
    trashScenarioFacade.backAll(projectId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Query scenario trash count",
      description = "Get the total count of scenarios currently in the recycle bin for the project",
      operationId = "scenario:trash:count")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario trash count retrieved successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/count")
  public ApiLocaleResult<Long> count(
      @RequestParam("projectId") @Parameter(name = "projectId", description = "Project identifier for trash count") Long projectId) {
    return ApiLocaleResult.success(trashScenarioFacade.count(projectId));
  }

  @Operation(summary = "Query scenario trash list",
      description = "Retrieve paginated list of deleted scenarios in the recycle bin with filtering capabilities",
      operationId = "scenario:trash:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scenario trash list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ScenarioTrashDetailVo>> list(
      @Valid @ParameterObject ScenarioTrashListDto dto) {
    return ApiLocaleResult.success(trashScenarioFacade.list(dto));
  }

}
