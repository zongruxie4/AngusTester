package cloud.xcan.angus.core.tester.interfaces.script;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.api.tester.script.ScriptDetailVo;
import cloud.xcan.angus.api.tester.script.dto.ScriptFindDto;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoListVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfoVo;
import cloud.xcan.angus.api.tester.script.vo.ScriptInfosVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptFacade;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Script - Internal", description = "Script Internal Management API - Internal system interfaces for programmatic script metadata retrieval and execution record management.")
@Validated
@RestController
@RequestMapping("/innerapi/v1/script")
public class ScriptInnerRest {

  @Resource
  private ScriptFacade scriptFacade;

  @Operation(summary = "Query script detail",
      description = "Retrieve comprehensive script configuration and metadata for internal processing.",
      operationId = "script:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script detail retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @Parameter(name = "id", description = "Script identifier for detail query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @Operation(summary = "Query script info",
      description = "Retrieve script information and metadata for internal system integration.",
      operationId = "script:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Script not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @Parameter(name = "id", description = "Script identifier for info query", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(summary = "Query multiple scripts info",
      description = "Retrieve information for multiple scripts in batch for internal processing.",
      operationId = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Scripts info retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more scripts not found")})
  //@GetMapping(value = "/innerapi/v1/script/infos") -> Conflicted with '/innerapi/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @Parameter(name = "ids", description = "Script identifiers for batch info query", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @Operation(summary = "Query script info list",
      description = "Retrieve paginated list of script information for internal system management.",
      operationId = "script:info:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Script info list retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "No scripts found")})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(
      @Valid @ParameterObject ScriptFindDto dto) {
    return ApiLocaleResult.success(scriptFacade.infoList(dto));
  }

}
