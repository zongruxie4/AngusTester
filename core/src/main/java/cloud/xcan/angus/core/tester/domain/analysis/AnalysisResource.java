package cloud.xcan.angus.core.tester.domain.analysis;

import cloud.xcan.angus.spec.experimental.Value;

public enum AnalysisResource implements Value<String> {
  TASK, CASE;

  public boolean isTask() {
    return this == AnalysisResource.TASK;
  }

  public boolean isCase() {
    return this == AnalysisResource.CASE;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
