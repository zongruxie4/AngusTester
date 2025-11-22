package cloud.xcan.angus.core.tester.domain.report;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ReportResourcesCount {

  @Schema(description = "Total number of report")
  private long allReport;

  private LinkedHashMap<ReportCategory, Long> reportByCategory = new LinkedHashMap<>();

}
