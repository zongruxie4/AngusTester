package cloud.xcan.angus.core.tester.domain.analysis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AnalysisDataSource implements EnumMessage<String> {
  REAL_TIME_DATA, SNAPSHOT_DATA;

  public boolean isSnapshotData() {
    return this.equals(SNAPSHOT_DATA);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
