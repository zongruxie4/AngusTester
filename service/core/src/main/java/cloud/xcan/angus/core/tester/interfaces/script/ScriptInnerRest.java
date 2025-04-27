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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ScriptInner", description = "Script Query (Internal) - Internal system APIs for programmatic retrieval of test script metadata and execution records.")
@Validated
@RestController
@RequestMapping("/innerapi/v1/script")
public class ScriptInnerRest {

  @Resource
  private ScriptFacade scriptFacade;

  @Operation(description = "Query the detail of script", operationId = "script:detail:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @Operation(description = "Query the info of script", operationId = "script:info:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @Parameter(name = "id", description = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @Operation(description = "Query the info of script", operationId = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  //@GetMapping(value = "/innerapi/v1/script/infos") -> Conflicted with '/innerapi/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @Parameter(name = "ids", description = "Script ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @Operation(description = "Query the list of script info", operationId = "script:info:list:inner")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Resource not found")})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(@Valid ScriptFindDto dto) {
    return ApiLocaleResult.success(scriptFacade.infoList(dto));
  }

}
