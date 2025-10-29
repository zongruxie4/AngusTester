package cloud.xcan.angus.core.tester.domain.issue.count;

public interface LeadTimeCountBase {

  long getUserNum();

  void setUserNum(long userNum);

  double getUserAvgProcessingTime();

  void setUserAvgProcessingTime(double userAvgProcessingTime);

  double getTotalProcessingTime();

  void setTotalProcessingTime(double totalProcessingTime);

  double getAvgProcessingTime();

  void setAvgProcessingTime(double avgProcessingTime);

  double getMinProcessingTime();

  void setMinProcessingTime(double minProcessingTime);

  double getMaxProcessingTime();

  void setMaxProcessingTime(double maxProcessingTime);

  double getP50ProcessingTime();

  void setP50ProcessingTime(double p50ProcessingTime);

  double getP75ProcessingTime();

  void setP75ProcessingTime(double p75ProcessingTime);

  double getP90ProcessingTime();

  void setP90ProcessingTime(double p90ProcessingTime);

  double getP95ProcessingTime();

  void setP95ProcessingTime(double p95ProcessingTime);

  double getP99ProcessingTime();

  void setP99ProcessingTime(double p99ProcessingTime);

}
