package cloud.xcan.angus.core.tester.interfaces.activity;


import cloud.xcan.angus.core.tester.interfaces.activity.facade.ActivityFacade;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.vo.ActivityDetailVo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.remote.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Activity", description = "User Activity Management - Comprehensive APIs for querying and analyzing user activity logs including resource access patterns, operational events, and audit trail management with granular filtering and export capabilities")
@Validated
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityRest {

  @Resource
  private ActivityFacade activityFacade;

  @Operation(summary = "Activity list query", 
      description = "Query and retrieve comprehensive activity logs with advanced filtering options and pagination support",
      operationId = "activity:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Activity list retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ActivityDetailVo>> list(
      @Valid @ParameterObject ActivityFindDto dto) {
    return ApiLocaleResult.success(activityFacade.list(dto));
  }

}
