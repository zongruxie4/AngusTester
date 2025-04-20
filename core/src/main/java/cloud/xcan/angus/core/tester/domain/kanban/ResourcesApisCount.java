package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResourcesApisCount {

  @Schema(description = "Total number of service")
  private long allService;
  @Schema(description = "The number of services group by status")
  private LinkedHashMap<ApiStatus, Integer> servicesByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of apis")
  private long allApis;
  @Schema(description = "The number of apis group by status")
  private LinkedHashMap<ApiStatus, Integer> apisByStatus = new LinkedHashMap<>();
  @Schema(description = "The number of apis group by method")
  private LinkedHashMap<HttpMethod, Integer> apisByMethod = new LinkedHashMap<>();

  @Schema(description = "Total number of unarchived apis")
  private long allUnarchivedApis;


}
