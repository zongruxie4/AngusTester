package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
