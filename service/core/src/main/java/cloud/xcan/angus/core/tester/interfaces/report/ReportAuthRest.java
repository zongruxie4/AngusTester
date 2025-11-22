package cloud.xcan.angus.core.tester.interfaces.report;


import cloud.xcan.angus.core.spring.condition.NotCommunityEditionCondition;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.interfaces.report.facade.ReportAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth.ReportAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth.ReportAuthVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
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

@Tag(name = "Report Authorization", description = "Report Authorization Management - "
    + "Unified entry for managing data access permissions of reports, "
    + "controlling who can view, edit, or manage specific report content")
@Validated
@RestController
@RequestMapping("/api/v1/report")
@Conditional(NotCommunityEditionCondition.class)
public class ReportAuthRest {

  @Resource
  private ReportAuthFacade reportAuthFacade;

  @Operation(summary = "Add report authorization", description = "Grant authorization permissions to users or groups for accessing a specific report", operationId = "report:auth:add")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Report authorization added successfully")})
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = "/{id}/auth")
  public ApiLocaleResult<IdKey<Long, Object>> add(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId,
      @Valid @RequestBody ReportAuthAddDto dto) {
    return ApiLocaleResult.success(reportAuthFacade.add(reportId, dto));
  }

  @Operation(summary = "Replace report authorization", description = "Completely replace the authorization configuration for a specific report", operationId = "report:auth:replace")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report authorization replaced successfully"),
      @ApiResponse(responseCode = "404", description = "Report authorization not found")
  })
  @PutMapping("/auth/{id}")
  public ApiLocaleResult<?> replace(
      @Parameter(name = "id", description = "Report authorization ID", required = true) @PathVariable("id") Long id,
      @Valid @RequestBody ReportAuthReplaceDto dto) {
    reportAuthFacade.replace(id, dto);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Enable or disable report authorization", description = "Toggle the enabled status of report authorization to control access", operationId = "report:auth:enabled")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report authorization status updated successfully")})
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/{id}/auth/enabled")
  public ApiLocaleResult<?> enabled(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId,
      @Valid @NotNull @Parameter(name = "enabled", description = "Enable or disable authorization", required = true) @RequestParam(value = "enabled") Boolean enabled) {
    reportAuthFacade.enabled(reportId, enabled);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "Check report authorization status", description = "Verify whether authorization is enabled for a specific report", operationId = "report:auth:status")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report authorization status retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/auth/status")
  public ApiLocaleResult<Boolean> status(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId) {
    return ApiLocaleResult.success(reportAuthFacade.status(reportId));
  }

  @Operation(summary = "Delete report authorization", description = "Remove specific authorization permissions for a report", operationId = "report:auth:delete")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Report authorization deleted successfully")})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/auth/{id}")
  public void delete(
      @Parameter(name = "id", description = "Report authorization ID", required = true) @PathVariable("id") Long id) {
    reportAuthFacade.delete(id);
  }

  @Operation(summary = "Get user report permissions", description = "Retrieve all authorization permissions for a specific user on a report", operationId = "report:user:auth")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth")
  public ApiLocaleResult<List<ReportPermission>> userAuth(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId,
      @Parameter(name = "userId", description = "User ID", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "admin", description = "Required when querying administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(reportAuthFacade.userAuth(reportId, userId, admin));
  }

  @Operation(summary = "Get current user report permissions", description = "Retrieve authorization permissions for the currently authenticated user on a specific report", operationId = "report:user:auth:current")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user permissions retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Report not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/auth/current")
  public ApiLocaleResult<ReportAuthCurrentVo> currentUserAuth(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId,
      @Parameter(name = "admin", description = "Required when querying administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(reportAuthFacade.currentUserAuth(reportId, admin));
  }

  @Operation(summary = "Get current user permissions for multiple reports", description = "Batch retrieve current user authorization permissions for multiple reports", operationId = "report:user:auth:current:batch")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Current user permissions for multiple reports retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "One or more reports not found")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/user/auth/current")
  public ApiLocaleResult<Map<Long, ReportAuthCurrentVo>> currentUserAuths(
      @Parameter(name = "ids", description = "List of report IDs", required = true) @RequestParam(value = "ids") @NotEmpty HashSet<Long> reportIds,
      @Parameter(name = "admin", description = "Required when querying administrator permissions") Boolean admin) {
    return ApiLocaleResult.success(reportAuthFacade.currentUserAuths(reportIds, admin));
  }

  @Operation(summary = "Verify user authorization permission", description = "Check if a specific user has required authorization permission or administrator access for a report", operationId = "report:auth:check")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User authorization verified successfully")})
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{id}/user/{userId}/auth/{authPermission}/check")
  public ApiLocaleResult<?> authCheck(
      @Parameter(name = "id", description = "Report ID", required = true) @PathVariable("id") Long reportId,
      @Parameter(name = "userId", description = "User ID to check authorization", required = true) @PathVariable("userId") Long userId,
      @Parameter(name = "authPermission", description = "Required authorization permission level", required = true) @PathVariable("authPermission") ReportPermission permission) {
    reportAuthFacade.authCheck(reportId, permission, userId);
    return ApiLocaleResult.success();
  }

  @Operation(summary = "List report authorizations", description = "Retrieve paginated list of all report authorizations with filtering support", operationId = "report:auth:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Report authorizations list retrieved successfully")})
  @GetMapping("/auth")
  public ApiLocaleResult<PageResult<ReportAuthVo>> list(
      @Valid @ParameterObject ReportAuthFindDto dto) {
    return ApiLocaleResult.success(reportAuthFacade.list(dto));
  }

}
