package cloud.xcan.sdf.core.angustester.interfaces.activity;

import cloud.xcan.sdf.api.ApiLocaleResult;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.ActivityFacade;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.vo.ActivityDetailVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "Activity")
@Validated
@RestController
@RequestMapping("/api/v1/activity")
public class ActivityRest {

  @Resource
  private ActivityFacade activityFacade;

  @ApiOperation(value = "Query the list of activity", nickname = "activity:list")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping
  public ApiLocaleResult<PageResult<ActivityDetailVo>> list(@Valid ActivityFindDto dto) {
    return ApiLocaleResult.success(activityFacade.list(dto));
  }

  @ApiOperation(value = "Fulltext search the list of activity", nickname = "activity:search")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Retrieved successfully", response = ApiLocaleResult.class)})
  @GetMapping("/search")
  public ApiLocaleResult<PageResult<ActivityDetailVo>> search(@Valid ActivitySearchDto dto) {
    return ApiLocaleResult.success(activityFacade.search(dto));
  }

}