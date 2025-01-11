package cloud.xcan.sdf.core.angustester.interfaces.apis;


import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share.ApisShareVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaolong.liu
 */
@Api(tags = "ApisShare")
@Validated
@RestController
@RequestMapping("/api/v1/apis/share")
public class ApisShareRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @ApiOperation(value = "Add the sharing of apis", nickname = "apis:share:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<ApisShareAddVo> add(@Valid @RequestBody ApisShareAddDto dto) {
    return ApiLocaleResult.success(apisShareFacade.add(dto));
  }

  @ApiOperation(value = "Update the sharing of apis", nickname = "apis:share:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ApisShareUpdateDto dto) {
    apisShareFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete the sharing of apis", nickname = "apis:share:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @DeleteMapping
  public void delete(
      @ApiParam(name = "ids", value = "Apis sharing ids", required = true)
      @Valid @NotEmpty @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisShareFacade.delete(ids);
  }

  @ApiOperation(value = "Query the sharing detail of apis", nickname = "space:share:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Sharing does not exist", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisShareVo> detail(
      @ApiParam(name = "id", value = "Share id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisShareFacade.detail(id));
  }

  @ApiOperation(value = "Query the sharing list of apis", nickname = "apis:share:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisShareVo>> list(@Valid ApisShareFindDto dto) {
    return ApiLocaleResult.success(apisShareFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the sharing of apis", nickname = "apis:share:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisShareVo>> search(@Valid ApisShareSearchDto dto) {
    return ApiLocaleResult.success(apisShareFacade.search(dto));
  }

}
