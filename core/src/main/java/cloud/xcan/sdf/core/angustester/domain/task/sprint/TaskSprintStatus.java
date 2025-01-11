package cloud.xcan.sdf.core.angustester.domain.task.sprint;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum TaskSprintStatus implements EnumMessage<String> {
  PENDING, IN_PROGRESS, COMPLETED, BLOCKED;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isCompleted() {
    return this.equals(COMPLETED);
  }

  public boolean allowStart() {
    return PENDING.equals(this) || COMPLETED.equals(this) || BLOCKED.equals(this);
  }

  public boolean allowEnd() {
    return IN_PROGRESS.equals(this);
  }

  public boolean allowBlock() {
    return PENDING.equals(this) || IN_PROGRESS.equals(this);
  }

  public boolean isInProcess() {
    return this.equals(IN_PROGRESS);
  }

  public boolean isStarted() {
    return !this.equals(PENDING);
  }
}
