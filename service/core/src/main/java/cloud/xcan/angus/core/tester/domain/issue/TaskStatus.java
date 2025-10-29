package cloud.xcan.angus.core.tester.domain.issue;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;


@EndpointRegister
public enum TaskStatus implements EnumMessage<String> {
  PENDING, IN_PROGRESS, CONFIRMING, COMPLETED, CANCELED;

  public static boolean isFinished(TaskStatus status) {
    return CANCELED.equals(status) || COMPLETED.equals(status);
  }

  public boolean isFinished() {
    return CANCELED.equals(this) || COMPLETED.equals(this);
  }

  public boolean isPending() {
    return this.equals(PENDING);
  }

  public boolean isInProgress() {
    return this.equals(IN_PROGRESS);
  }

  public boolean canAutoUpdateResult() {
    return this.equals(IN_PROGRESS) || this.equals(CONFIRMING);
  }

  public boolean isConfirming() {
    return this.equals(CONFIRMING);
  }

  public boolean isCompleted() {
    return this.equals(COMPLETED);
  }

  public boolean isCanceled() {
    return this.equals(CANCELED);
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public static TaskStatus ofMessage(String massage, Locale locale) {
    for (TaskStatus value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(massage.trim())) {
        return value;
      }
    }
    throw new ProtocolException("Unknown task status: " + massage);
  }
}
