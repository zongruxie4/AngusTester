package cloud.xcan.sdf.core.angustester.interfaces.script;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.script.ScriptDetailVo;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptFindDto;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoListVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfosVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.ScriptFacade;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "ScriptDoor")
@Validated
@RestController
@RequestMapping("/doorapi/v1/script")
public class ScriptDoorRest {

  @Resource
  private ScriptFacade scriptFacade;

  @ApiOperation(value = "Query the detail of script", nickname = "script:detail:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ScriptDetailVo> detail(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.detail(id));
  }

  @ApiOperation(value = "Query the info of script", nickname = "script:info:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}/info")
  public ApiLocaleResult<ScriptInfoVo> info(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.info(id));
  }

  @ApiOperation(value = "Query the info of script", nickname = "script:infos")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  //@GetMapping(value = "/doorapi/v1/script/infos") -> Conflicted with '/doorapi/v1/script/{id}'
  @GetMapping(value = "/info/byids")
  public ApiLocaleResult<List<ScriptInfosVo>> infos(
      @ApiParam(name = "ids", value = "Script ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    return ApiLocaleResult.success(scriptFacade.infos(ids));
  }

  @ApiOperation(value = "Query the list of script info", nickname = "script:info:list:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping("/info")
  public ApiLocaleResult<PageResult<ScriptInfoListVo>> infoList(@Valid ScriptFindDto dto) {
    return ApiLocaleResult.success(scriptFacade.infoList(dto));
  }

}