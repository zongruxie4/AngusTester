package cloud.xcan.sdf.core.angustester.domain.kanban;

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
public class ResourcesApisCount {

  @ApiModelProperty(value = "Total number of service")
  private long allService;
  @ApiModelProperty(value = "The number of services group by status")
  private LinkedHashMap<ApiStatus, Integer> servicesByStatus = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of apis")
  private long allApis;
  @ApiModelProperty(value = "The number of apis group by status")
  private LinkedHashMap<ApiStatus, Integer> apisByStatus = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of apis group by method")
  private LinkedHashMap<HttpMethod, Integer> apisByMethod = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of unarchived apis")
  private long allUnarchivedApis;


}
