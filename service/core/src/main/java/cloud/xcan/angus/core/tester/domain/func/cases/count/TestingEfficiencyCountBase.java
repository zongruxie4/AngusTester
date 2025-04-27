package cloud.xcan.angus.core.tester.domain.func.cases.count;

public interface TestingEfficiencyCountBase {

  long getPassedTestNum();

  void setPassedTestNum(long passedTestNum);

  double getPassedTestRate();

  void setPassedTestRate(double passedTestRate);

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

  double calcPassedTestRate();

  double calcOneTimePassedRate();

  double calcTwoTimePassedRate();

  double calcOneTimeNotPassedRate();

}
