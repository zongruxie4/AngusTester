package cloud.xcan.angus.core.tester.domain.issue.count;

public interface UnplannedWorkCountBase {

  long getUnplannedNum();

  void setUnplannedNum(long unplannedNum);

  double getUnplannedRate();

  void setUnplannedRate(double unplannedRate);

  long getUnplannedCompletedNum();

  void setUnplannedCompletedNum(long unplannedCompletedNum);

  double getUnplannedCompletedRate();

  void setUnplannedCompletedRate(double unplannedCompletedRate);

  double getUnplannedWorkload();

  void setUnplannedWorkload(double unplannedWorkload);

  double getUnplannedWorkloadRate();

  void setUnplannedWorkloadRate(double unplannedWorkloadRate);

  double getUnplannedWorkloadCompleted();

  void setUnplannedWorkloadCompleted(double unplannedWorkloadCompleted);

  double getUnplannedWorkloadCompletedRate();

  void setUnplannedWorkloadCompletedRate(double unplannedWorkloadCompletedRate);

  double getDailyProcessedWorkload();

  void setDailyProcessedWorkload(double dailyProcessedWorkload);

  double getUnplannedWorkloadProcessingTime();

  void setUnplannedWorkloadProcessingTime(double unplannedWorkloadProcessingTime);

  double calcUnplannedRate();

  double calcUnplannedCompletedRate();

  double calcUnplannedWorkloadRate();

  double calcUnplannedWorkloadCompletedRate();

}
