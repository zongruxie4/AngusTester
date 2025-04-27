package cloud.xcan.angus.core.tester.domain.apis.count;

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
public class ApisResourcesCreationCount {

  @Schema(description = "Total number of service")
  private long allService;
  @Schema(description = "The number of new services added in the last one week")
  private long serviceByLastWeek;
  @Schema(description = "The number of new services added in the last one month")
  private long serviceByLastMonth;
  @Schema(description = "The number of services group by status")
  private LinkedHashMap<ApiStatus, Integer> servicesByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of apis")
  private long allApis;
  @Schema(description = "The number of new apis added in the last one week")
  private long apisByLastWeek;
  @Schema(description = "The number of new apis added in the last one month")
  private long apisByLastMonth;
  @Schema(description = "The number of apis group by status")
  private LinkedHashMap<ApiStatus, Integer> apisByStatus = new LinkedHashMap<>();
  @Schema(description = "The number of apis group by method")
  private LinkedHashMap<HttpMethod, Integer> apisByMethod = new LinkedHashMap<>();

  @Schema(description = "Total number of unarchived apis")
  private long allUnarchivedApis;
  @Schema(description = "The number of new unarchived apis added in the last one week")
  private long unarchivedApisByLastWeek;
  @Schema(description = "The number of new unarchived apis added in the last one month")
  private long unarchivedApisByLastMonth;

}
