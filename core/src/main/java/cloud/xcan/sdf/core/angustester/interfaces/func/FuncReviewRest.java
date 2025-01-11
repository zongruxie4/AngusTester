package cloud.xcan.sdf.core.angustester.interfaces.func;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncReviewFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Size;
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

@Api(tags = "FuncReview")
@Validated
@RestController
@RequestMapping("/api/v1/func/review")
public class FuncReviewRest {

  @Resource
  private FuncReviewFacade funcReviewFacade;

  @ApiOperation(value = "Add functional testing review", nickname = "func:review:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<IdKey<Long, Object>> add(@Valid @RequestBody FuncReviewAddDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.add(dto));
  }

  @ApiOperation(value = "Update functional testing review", nickname = "func:review:update")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody FuncReviewUpdateDto dto) {
    funcReviewFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Replace functional testing review", nickname = "func:review:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping
  public ApiLocaleResult<IdKey<Long, Object>> replace(
      @Valid @RequestBody FuncReviewReplaceDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.replace(dto));
  }

  @ApiOperation(value = "Start functional testing review", nickname = "func:review:start")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Started successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/start")
  public ApiLocaleResult<?> start(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.start(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "End functional testing review", nickname = "func:review:end")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "End successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/end")
  public ApiLocaleResult<?> end(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.end(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Block functional testing review", nickname = "func:review:block")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Block successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/block")
  public ApiLocaleResult<?> block(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.block(id);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Clone functional testing review", nickname = "func:review:clone")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Cloned successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping("/{id}/clone")
  public ApiLocaleResult<IdKey<Long, Object>> clone(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.clone(id));
  }

  @ApiOperation(value = "Reset the result of review result", nickname = "func:review:case:result:reset")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/case/review/reset")
  public ApiLocaleResult<?> reviewReset(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewReset(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Restart the result of review result", nickname = "func:review:case:result:restart")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Updated successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PatchMapping(value = "/case/review/restart")
  public ApiLocaleResult<?> reviewRestart(
      @Valid @Size(max = DEFAULT_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    funcReviewFacade.reviewRestart(ids);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Delete functional testing review", nickname = "func:review:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    funcReviewFacade.delete(id);
  }

  @ApiOperation(value = "Query the detail of functional testing review", nickname = "func:review:detail")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<FuncReviewDetailVo> detail(
      @ApiParam(name = "id", value = "Review id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(funcReviewFacade.detail(id));
  }

  @ApiOperation(value = "Query the list of functional testing review", nickname = "func:review:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<FuncReviewDetailVo>> list(@Valid FuncReviewFindDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of functional testing review", nickname = "func:review:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<FuncReviewDetailVo>> search(@Valid FuncReviewSearchDto dto) {
    return ApiLocaleResult.success(funcReviewFacade.search(dto));
  }

}
