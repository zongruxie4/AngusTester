package cloud.xcan.sdf.api.angustester.script;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.script.dto.ScriptFindDto;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoListVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfoVo;
import cloud.xcan.sdf.api.angustester.script.vo.ScriptInfosVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author xiaolong.liu
 */
@FeignClient(name = "${xcan.service.angustester:XCAN-ANGUSTESTER.BOOT}")
public interface ScriptDoorRemote {

  @ApiOperation(value = "Query the detail of script", nickname = "script:detail:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/doorapi/v1/script/{id}")
  ApiLocaleResult<ScriptDetailVo> detail(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id);

  @ApiOperation(value = "Query the info of script", nickname = "script:info:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/doorapi/v1/script/{id}/info")
  ApiLocaleResult<ScriptInfoVo> info(
      @ApiParam(name = "id", value = "Script id", required = true) @PathVariable("id") Long id);

  @ApiOperation(value = "Query the info of scripts", nickname = "script:infos:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  //@GetMapping(value = "/doorapi/v1/script/infos") -> Conflicted with '/doorapi/v1/script/{id}'
  @GetMapping(value = "/doorapi/v1/script/info/byids")
  ApiLocaleResult<List<ScriptInfosVo>> infos(
      @ApiParam(name = "ids", value = "Script ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids);

  @ApiOperation(value = "Query the list of script info", nickname = "script:info:list:door")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/doorapi/v1/script/info")
  ApiLocaleResult<PageResult<ScriptInfoListVo>> list(@SpringQueryMap ScriptFindDto dto);

}
