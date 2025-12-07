package cloud.xcan.angus.core.tester.domain.exec.result;

public interface ExecTestResultBase {

  void setPassed(Boolean passed);

  void setFailureMessage(String failureMessage);

  void setUsageFailedNodeId(Long usageFailedNodeId);

}
