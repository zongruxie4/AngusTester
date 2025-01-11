package cloud.xcan.sdf.core.angustester.interfaces.report;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.ReportAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.sdf.core.spring.condition.NotPrivateEditionCondition;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.context.annotation.Conditional;
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

@Api(tags = "ReportAuth")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotPrivateEditionCondition.class)
public class ReportAuthRest {

  @Resource
  private ReportAuthFacade reportAuthFacade;

  @ApiOperation(value = "Add the authorization of report", nickname = "report:auth:add")
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "Created successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId,
      @Valid @RequestBody ReportAuthAddDto dto) {
    return ApiLocaleResult.success(reportAuthFacade.add(reportId, dto));
  }

  @ApiOperation(value = "Replace the authorization of report", nickname = "report:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Replaced successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @ApiParam(name = "id", value = "Report authorization id", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ReportAuthReplaceDto dto) {
    reportAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Enable or disable the authorization of report", nickname = "report:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Enabled or disabled successfully", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId,
      @Valid @NotNull @ApiParam(name = "enabled", value = "Enabled(true) or Disabled(false)", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    reportAuthFacade.enabled(reportId, enabled);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query authorization status of report", nickname = "report:auth:status")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId) {
    return ApiLocaleResult.success(reportAuthFacade.status(reportId));
  }

  @ApiOperation(value = "Delete the authorization of report", nickname = "report:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @ApiParam(name = "id", value = "Report authorization id", required = true) @PathVariable("id") Long id) {
    reportAuthFacade.delete(id);
  }

  @ApiOperation(value = "Query the user authorization permission of report", nickname = "report:user:auth")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ReportPermission>> userAuth(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId,
      @ApiParam(name = "userId", value = "userId", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(reportAuthFacade.userAuth(reportId, userId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission of report", nickname = "report:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ReportAuthCurrentVo> currentUserAuth(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(reportAuthFacade.currentUserAuth(reportId, adminFlag));
  }

  @ApiOperation(value = "Query the current user authorization permission", nickname = "report:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class),
      @ApiResponse(code = 404, message = "Resource not found", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, ReportAuthCurrentVo>> currentUserAuths(
      @ApiParam(name = "ids", value = "Report ids", required = true) @RequestParam(value = "ids") @NotEmpty HashSet<Long> reportIds,
      @ApiParam(name = "adminFlag", value = "Required when the query contains administrator permissions") Boolean adminFlag) {
    return ApiLocaleResult.success(reportAuthFacade.currentUserAuths(reportIds, adminFlag));
  }

  @ApiOperation(value = "Check the user authorization permission of report, the administrator permission is included", nickname = "report:auth:check")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Resource existed", response = ApiLocaleResult.class)})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @ApiParam(name = "id", value = "Report id", required = true) @PathVariable("id") Long reportId,
      @ApiParam(name = "userId", value = "Authorization user id", required = true) @PathVariable("userId") Long userId,
      @ApiParam(name = "authPermission", value = "Report authorized permission", required = true) @PathVariable("authPermission") ReportPermission permission) {
    reportAuthFacade.authCheck(reportId, permission, userId);
    return ApiLocaleResult.success();
  }

  @ApiOperation(value = "Query the list of report authorization", nickname = "report:auth:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ReportAuthVo>> list(@Valid ReportAuthFindDto dto) {
    return ApiLocaleResult.success(reportAuthFacade.list(dto));
  }

}
