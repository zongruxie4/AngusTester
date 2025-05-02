package cloud.xcan.angus.api.commonlink.exec;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;


@Getter
@EndpointRegister
public enum ExecStatus implements EnumMessage<String> {
  CREATED, PENDING, RUNNING, STOPPED, FAILED, COMPLETED, TIMEOUT;

  public boolean isCreated() {
    return this.equals(CREATED);
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isRunning() {
    return this.equals(RUNNING);
  }

  public boolean isStopped() {
    return this.equals(STOPPED);
  }

  public boolean isCompleted() {
    return this.equals(COMPLETED);
  }

  public boolean isWideRunning() {
    return this.equals(PENDING) || this.equals(RUNNING);
  }

  public boolean isWideStopped() {
    return this.equals(STOPPED) || this.equals(FAILED) || this.equals(COMPLETED)
        || this.equals(TIMEOUT);
  }

  public boolean isWideFailed() {
    return this.equals(FAILED) || this.equals(TIMEOUT);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
