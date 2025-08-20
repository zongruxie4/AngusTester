package cloud.xcan.angus.core.tester.interfaces.apis;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;

import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisShareFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share.ApisShareFindDto;
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

@Tag(name = "APIs Sharing", description = "APIs Sharing Management - APIs for unified management of service and API resource sharing with access control and collaboration features")
@Validated
@RestController
@RequestMapping("/api/v1/apis/share")
public class ApisShareRest {

  @Resource
  private ApisShareFacade apisShareFacade;

  @Operation(summary = "Create API sharing", 
      description = "Create new API sharing with comprehensive access control and collaboration settings",
      operationId = "apis:share:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "API sharing created successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public ApiLocaleResult<ApisShareAddVo> add(@Valid @RequestBody ApisShareAddDto dto) {
    return ApiLocaleResult.success(apisShareFacade.add(dto));
  }

  @Operation(summary = "Update API sharing", 
      description = "Update existing API sharing with comprehensive configuration and access control",
      operationId = "apis:share:update")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API sharing updated successfully"),
      @ApiResponse(responseCode = "404", description = "API sharing not found")})
  @PatchMapping
  public ApiLocaleResult<?> update(@Valid @RequestBody ApisShareUpdateDto dto) {
    apisShareFacade.update(dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Delete API sharing", 
      description = "Remove API sharing from the system with proper cleanup and access control validation",
      operationId = "apis:share:delete")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "API sharing deleted successfully"),
      @ApiResponse(responseCode = "404", description = "API sharing not found")})
  @DeleteMapping
  public void delete(
      @Parameter(name = "ids", description = "API sharing identifiers for batch deletion", required = true)
      @Valid @NotEmpty @Size(max = MAX_BATCH_SIZE) @RequestParam("ids") HashSet<Long> ids) {
    apisShareFacade.delete(ids);
  }

  @Operation(summary = "Get API sharing details", 
      description = "Retrieve comprehensive API sharing details including access control and collaboration settings",
      operationId = "space:share:detail")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API sharing details retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "API sharing not found")})
  @GetMapping(value = "/{id}")
  public ApiLocaleResult<ApisShareVo> detail(
      @Parameter(name = "id", description = "API sharing identifier for detail retrieval", required = true) @PathVariable("id") Long id) {
    return ApiLocaleResult.success(apisShareFacade.detail(id));
  }

  @Operation(summary = "Query API sharing list", 
      description = "Retrieve paginated list of API sharing with comprehensive filtering and search options",
      operationId = "apis:share:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "API sharing list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ApisShareVo>> list(
      @Valid @ParameterObject ApisShareFindDto dto) {
    return ApiLocaleResult.success(apisShareFacade.list(dto));
  }

}
