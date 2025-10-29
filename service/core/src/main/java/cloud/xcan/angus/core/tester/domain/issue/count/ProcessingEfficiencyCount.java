package cloud.xcan.angus.core.tester.domain.issue.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessingEfficiencyCount implements ProcessingEfficiencyCountBase {

  @Schema(description = "Total number of work, Ignoring cancel status tasks")
  protected long totalNum;

  @Schema(description = "Completed number")
  protected long completedNum; // Avoiding duplicate definitions
  @Schema(description = "The rate of completed work, equal to the progress")
  protected double completedRate;
  @Schema(description = "The number of tasks that the passed at one time")
  protected long oneTimePassedNum;
  @Schema(description = "One-time pass rate")
  protected double oneTimePassedRate = 0d;
  @Schema(description = "The number of tasks that the passed at two time")
  protected long twoTimePassedNum;
  @Schema(description = "Two-time pass rate")
  protected double twoTimePassedRate = 0d;
  @Schema(description = "The number of tasks that the passed not at one time")
  protected long oneTimeNotPassedNum;
  @Schema(description = "One-time not pass rate")
  protected double oneTimeNotPassedRate = 0d;

  public double calcComplatedRate() {
    return calcRate(completedNum, totalNum);
  }

  public double calcOneTimePassedRate() {
    return calcRate(oneTimePassedNum, completedNum);
  }

  public double calcTwoTimePassedRate() {
    return calcRate(twoTimePassedNum, completedNum);
  }

  public double calcOneTimeNotPassedRate() {
    return calcRate(oneTimeNotPassedNum, completedNum);
  }

}
