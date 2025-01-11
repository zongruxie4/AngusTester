package cloud.xcan.sdf.core.angustester.domain.apis.count;

import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisResourcesCreationCount {

  @ApiModelProperty(value = "Total number of service")
  private long allService;
  @ApiModelProperty(value = "The number of new services added in the last one week")
  private long serviceByLastWeek;
  @ApiModelProperty(value = "The number of new services added in the last one month")
  private long serviceByLastMonth;
  @ApiModelProperty(value = "The number of services group by status")
  private LinkedHashMap<ApiStatus, Integer> servicesByStatus = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of apis")
  private long allApis;
  @ApiModelProperty(value = "The number of new apis added in the last one week")
  private long apisByLastWeek;
  @ApiModelProperty(value = "The number of new apis added in the last one month")
  private long apisByLastMonth;
  @ApiModelProperty(value = "The number of apis group by status")
  private LinkedHashMap<ApiStatus, Integer> apisByStatus = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of apis group by method")
  private LinkedHashMap<HttpMethod, Integer> apisByMethod = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of unarchived apis")
  private long allUnarchivedApis;
  @ApiModelProperty(value = "The number of new unarchived apis added in the last one week")
  private long unarchivedApisByLastWeek;
  @ApiModelProperty(value = "The number of new unarchived apis added in the last one month")
  private long unarchivedApisByLastMonth;

}
