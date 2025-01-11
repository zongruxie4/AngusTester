package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewEfficiencyCount implements ReviewEfficiencyCountBase {

  @ApiModelProperty(value = "Total number of enabled review and valid cases")
  protected long totalNum;

  @ApiModelProperty(value = "Completed number")
  protected long passedReviewNum; // Avoiding duplicate definitions
  @ApiModelProperty(value = "The rate of completed work, equal to the progress")
  protected double passedReviewRate;
  @ApiModelProperty(value = "The number of cases that the Review passed at one time")
  protected long oneTimePassedReviewNum;
  @ApiModelProperty(value = "One-time review pass rate")
  protected double oneTimePassedReviewRate = 0d;
  @ApiModelProperty(value = "The number of cases that the passed review at two time")
  protected long twoTimePassedReviewNum;
  @ApiModelProperty(value = "Two-time pass rate")
  protected double twoTimePassedReviewRate = 0d;
  @ApiModelProperty(value = "The number of cases that the passed review not at one time")
  protected long oneTimeNotPassedReviewNum;
  @ApiModelProperty(value = "One-time not pass review rate")
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
