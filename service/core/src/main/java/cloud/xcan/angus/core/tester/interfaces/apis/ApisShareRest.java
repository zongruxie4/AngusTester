package cloud.xcan.angus.core.tester.interfaces.apis;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareSearchDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareAddVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share.ApisShareVo;
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
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "ApisShare", description = "API Sharing Management - Unified management entrance for service and apis resource sharing.")
@Validated
@RestController
@RequestMapping("/api/v1/apis/share")
public class ApisShareRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @Operation(summary = "Add the sharing of apis", operationId = "apis:share:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<ApisShareAddVo> add(@Valid @RequestBody ApisShareAddDto dto) {
    return ApiLocaleResult.success(apisShareFacade.add(dto));
  }

  @Operation(summary = "Update the sharing of apis", operationId = "apis:share:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Updated successfully"),
      @ApiResponse(responseCode = "404", description = "Sharing does not exist")})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ApisShareUpdateDto dto) {
    apisShareFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete the sharing of apis", operationId = "apis:share:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Sharing does not exist")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "Apis sharing ids", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisShareFacade.delete(ids);
  }

  @Operation(summary = "Query the sharing detail of apis", operationId = "space:share:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Sharing does not exist")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisShareVo> detail(
      @Parameter(name = "id", description = "Share id", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisShareFacade.detail(id));
  }

  @Operation(summary = "Query the sharing list of apis", operationId = "apis:share:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisShareVo>> list(
      @Valid @ParameterObject ApisShareFindDto dto) {
    return ApiLocaleResult.success(apisShareFacade.list(dto));
  }

  @Operation(summary = "Fulltext search the sharing of apis", operationId = "apis:share:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ApisShareVo>> search(
      @Valid @ParameterObject ApisShareSearchDto dto) {
    return ApiLocaleResult.success(apisShareFacade.search(dto));
  }

}
