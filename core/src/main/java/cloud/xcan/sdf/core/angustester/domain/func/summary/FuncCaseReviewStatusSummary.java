package cloud.xcan.sdf.core.angustester.domain.func.summary;

import cloud.xcan.sdf.api.enums.ReviewStatus;
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
