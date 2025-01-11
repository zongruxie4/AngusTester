package cloud.xcan.sdf.core.angustester.domain.task.count;

public interface ProcessingEfficiencyCountBase {

  long getCompletedNum();

  void setCompletedNum(long completedNum);

  double getCompletedRate();

  void setCompletedRate(double completedRate);

  long getOneTimePassedNum();

  void setOneTimePassedNum(long oneTimePassedNum);

  double getOneTimePassedRate();

  void setOneTimePassedRate(double oneTimePassedRate);

  long getTwoTimePassedNum();

  void setTwoTimePassedNum(long twoTimePassedNum);

  double getTwoTimePassedRate();

  void setTwoTimePassedRate(double twoTimePassedRate);

  long getOneTimeNotPassedNum();

  void setOneTimeNotPassedNum(long oneTimeNotPassedNum);

  double getOneTimeNotPassedRate();

  void setOneTimeNotPassedRate(double oneTimeNotPassedRate);

  double calcComplatedRate();

  double calcOneTimePassedRate();

  double calcTwoTimePassedRate();

  double calcOneTimeNotPassedRate();

}
