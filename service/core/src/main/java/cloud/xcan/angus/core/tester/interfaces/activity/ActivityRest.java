package cloud.xcan.angus.core.tester.interfaces.activity;


import cloud.xcan.angus.core.tester.interfaces.activity.facade.ActivityFacade;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.dto.ActivitySearchDto;
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

@Tag(name = "Activity", description = "User Activity Auditing - APIs for querying and analyzing user activity logs (resource access, and operational events) with granular filters and audit trail exports.")
@Validated
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityRest {

  @Resource
  private ActivityFacade activityFacade;

  @Operation(description = "Query the list of activity", operationId = "activity:list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping
  public ApiLocaleResult<PageResult<ActivityDetailVo>> list(@Valid @ParameterObject ActivityFindDto dto) {
    return ApiLocaleResult.success(activityFacade.list(dto));
  }

  @Operation(description = "Fulltext search the list of activity", operationId = "activity:search")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Retrieved successfully")})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ActivityDetailVo>> search(@Valid @ParameterObject ActivitySearchDto dto) {
    return ApiLocaleResult.success(activityFacade.search(dto));
  }

}
