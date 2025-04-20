package cloud.xcan.angus.core.tester.domain.task.count;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import java.util.LinkedHashMap;

public interface BugCountBase {

  long getBugNum();

  void setBugNum(long bugNum);

  double getBugRate();

  void setBugRate(double bugRate);

  long getValidBugNum();

  void setValidBugNum(long validBugNum);

  double getValidBugRate();

  void setValidBugRate(double invalidBugRate);

  long getInvalidBugNum();

  void setInvalidBugNum(long invalidBugNum);

  double getInvalidBugRate();

  void setInvalidBugRate(double validBugRate);

  double getBugWorkload();

  void setBugWorkload(double bugWorkload);

  double getBugWorkloadRate();

  void setBugWorkloadRate(double bugWorkloadRate);

  long getMissingBugNum();

  void setMissingBugNum(long missingBugNum);

  double getMissingBugRate();

  void setMissingBugRate(double missingBugRate);

  long getTotalPassedBugNum();

  void setTotalPassedBugNum(long totalPassedBugNum);

  long getOneTimePassedBugNum();

  void setOneTimePassedBugNum(long oneTimePassedBugNum);

  double getOneTimePassedBugRate();

  void setOneTimePassedBugRate(double oneTimePassedBugRate);

  LinkedHashMap<BugLevel, Integer> getBugLevelCount();

  void setBugLevelCount(LinkedHashMap<BugLevel, Integer> bugLevelCount);

  LinkedHashMap<BugLevel, Double> getBugLevelRate();

  void setBugLevelRate(LinkedHashMap<BugLevel, Double> bugLevelRate);

  double calcBugRate();

  double calcValidBugRate();

  double calcInvalidBugRate();

  double calcBugWorkloadRate();

  double calcMissingBugRate();

  double calcOneTimePassedBugRate();

}
