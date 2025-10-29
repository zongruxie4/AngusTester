package cloud.xcan.angus.core.tester.domain.issue.count;

import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import java.util.LinkedHashMap;

public interface FailureAssessmentCountBase {

  long getFailureNum();

  void setFailureNum(long failureNum);

  double getFailureWorkload();

  void setFailureWorkload(double failureWorkload);

  long getBugNum();

  void setBugNum(long bugNum);

  long getOneTimeFailureNum();

  void setOneTimeFailureNum(long oneTimeFailureNum);

  double getOneTimeFailureRate();

  void setOneTimeFailureRate(double oneTimeFailureRate);

  long getTwoTimeFailureNum();

  void setTwoTimeFailureNum(long twoTimeFailureNum);

  double getTwoTimeFailureRate();

  void setTwoTimeFailureRate(double twoTimeFailureRate);

  long getFailureCompletedNum();

  void setFailureCompletedNum(long failureCompletedNum);

  double getFailureCompletedRate();

  void setFailureCompletedRate(double failureCompletedRate);

  long getFailureOverdueNum();

  void setFailureOverdueNum(long failureOverdueNum);

  double getFailureOverdueRate();

  void setFailureOverdueRate(double failureOverdueRate);

  double getFailureTotalTime();

  void setFailureTotalTime(double failureTotalOverdueTime);

  double getFailureAvgTime();

  void setFailureAvgTime(double failureAvgOverdueTime);

  double getFailureMinTime();

  void setFailureMinTime(double failureMinOverdueTime);

  double getFailureMaxTime();

  void setFailureMaxTime(double failureMaxOverdueTime);

  LinkedHashMap<BugLevel, Integer> getFailureLevelCount();

  void setFailureLevelCount(LinkedHashMap<BugLevel, Integer> failureLevelCount);

  LinkedHashMap<BugLevel, Double> getFailureLevelRate();

  void setFailureLevelRate(LinkedHashMap<BugLevel, Double> failureLevelRate);

  double calcOneTimeFailureRate();

  double calcTwoTimeFailureRate();

  double calcFailureCompletedRate();

  double calcFailureOverdueRate();

}
