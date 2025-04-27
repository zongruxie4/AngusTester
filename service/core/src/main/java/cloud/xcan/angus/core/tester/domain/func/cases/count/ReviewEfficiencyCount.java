package cloud.xcan.angus.core.tester.domain.func.cases.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewEfficiencyCount implements ReviewEfficiencyCountBase {

  @Schema(description = "Total number of enabled review and valid cases")
  protected long totalNum;

  @Schema(description = "Completed number")
  protected long passedReviewNum; // Avoiding duplicate definitions
  @Schema(description = "The rate of completed work, equal to the progress")
  protected double passedReviewRate;
  @Schema(description = "The number of cases that the Review passed at one time")
  protected long oneTimePassedReviewNum;
  @Schema(description = "One-time review pass rate")
  protected double oneTimePassedReviewRate = 0d;
  @Schema(description = "The number of cases that the passed review at two time")
  protected long twoTimePassedReviewNum;
  @Schema(description = "Two-time pass rate")
  protected double twoTimePassedReviewRate = 0d;
  @Schema(description = "The number of cases that the passed review not at one time")
  protected long oneTimeNotPassedReviewNum;
  @Schema(description = "One-time not pass review rate")
  protected double oneTimeNotPassedReviewRate = 0d;

  public double calcPassedReviewRate() {
    return calcRate(passedReviewNum, totalNum);
  }

  public double calcOneTimePassedReviewRate() {
    return calcRate(oneTimePassedReviewNum, passedReviewNum);
  }

  public double calcTwoTimePassedReviewRate() {
    return calcRate(twoTimePassedReviewNum, passedReviewNum);
  }

  public double calcOneTimeNotPassedReviewRate() {
    return calcRate(oneTimeNotPassedReviewNum, passedReviewNum);
  }

}
