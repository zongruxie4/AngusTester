package cloud.xcan.angus.core.tester.domain.report.summary;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
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
