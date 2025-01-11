package cloud.xcan.sdf.core.angustester.domain.report;

import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ReportResourcesCount {

  @ApiModelProperty(value = "Total number of report")
  private long allReport;

  private LinkedHashMap<ReportCategory, Long> reportByCategory = new LinkedHashMap<>();

}
