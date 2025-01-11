package cloud.xcan.sdf.core.angustester.domain.task.count;

public interface WorkloadCountBase {

  double getEvalWorkload();

  void setEvalWorkload(double evalWorkload);

  double getActualWorkload();

  void setActualWorkload(double actualWorkload);

  double getCompletedWorkload();

  void setCompletedWorkload(double completedWorkload);

  double getCompletedWorkloadRate();

  void setCompletedWorkloadRate(double completedWorkloadRate);

  double getSavingWorkload();

  void setSavingWorkload(double savingWorkload);

  double getSavingWorkloadRate();

  void setSavingWorkloadRate(double savingWorkloadRate);

  double getInvalidWorkload();

  void setInvalidWorkload(double invalidWorkload);

  double getInvalidWorkloadRate();

  void setInvalidWorkloadRate(double invalidWorkloadRate);

  double calcCompletedWorkloadRate();

  double calcSavingWorkloadRate();

  double calcInvalidWorkloadRate();

}
