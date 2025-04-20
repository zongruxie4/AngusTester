package cloud.xcan.angus.core.tester.domain.func.cases.count;

public interface ReviewEfficiencyCountBase {

  long getPassedReviewNum();

  void setPassedReviewNum(long passedReviewNum);

  double getPassedReviewRate();

  void setPassedReviewRate(double passedReviewRate);

  long getOneTimePassedReviewNum();

  void setOneTimePassedReviewNum(long oneTimePassedNum);

  double getOneTimePassedReviewRate();

  void setOneTimePassedReviewRate(double oneTimePassedRate);

  long getTwoTimePassedReviewNum();

  void setTwoTimePassedReviewNum(long twoTimePassedNum);

  double getTwoTimePassedReviewRate();

  void setTwoTimePassedReviewRate(double twoTimePassedRate);

  long getOneTimeNotPassedReviewNum();

  void setOneTimeNotPassedReviewNum(long oneTimeNotPassedNum);

  double getOneTimeNotPassedReviewRate();

  void setOneTimeNotPassedReviewRate(double oneTimeNotPassedRate);

  double calcPassedReviewRate();

  double calcOneTimePassedReviewRate();

  double calcTwoTimePassedReviewRate();

  double calcOneTimeNotPassedReviewRate();

}
