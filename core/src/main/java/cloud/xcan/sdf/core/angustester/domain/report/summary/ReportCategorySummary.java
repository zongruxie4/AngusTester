package cloud.xcan.sdf.core.angustester.domain.report.summary;

import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportCategorySummary {

  private ReportCategory category;

  private long total;

}
