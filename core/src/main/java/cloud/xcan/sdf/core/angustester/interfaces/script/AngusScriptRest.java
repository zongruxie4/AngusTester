package cloud.xcan.sdf.core.angustester.interfaces.script;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.angustester.script.vo.AngusScriptDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.ScriptFacade;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "AngusScript")
@Validated
@RestController
@RequestMapping("/api/v1/angus/script")
public class AngusScriptRest {

  @Resource
  private ScriptFacade scriptFacade;

  @ApiOperation(value = "Add script by angus model", nickname = "script:angus:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> angusAdd(
      @RequestParam("projectId") @ApiParam(name = "projectId", value = "Project id") Long projectId,
      @Valid @RequestBody AngusScript dto) {
    return ApiLocaleResult.success(scriptFacade.angusAdd(projectId, dto));
  }

  @ApiOperation(value = "Replace script by angus model", nickname = "script:angus:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class)})
  @PutMapping("/{id}")
  public ApiLocaleResult<?> angusReplace(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody AngusScript dto) {
    scriptFacade.angusReplace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the script detail of angus model", nickname = "script:angus:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<AngusScriptDetailVo> angusDetail(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(scriptFacade.angusDetail(id));
  }
}
