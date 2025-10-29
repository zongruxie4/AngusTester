package cloud.xcan.angus.core.tester.domain.test.summary;

import cloud.xcan.angus.api.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FuncCaseReviewStatusSummary {

  private ReviewStatus status;

  private long total;

}
