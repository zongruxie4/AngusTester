package cloud.xcan.sdf.core.angustester.domain.task;

import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import java.util.Locale;

@EndpointRegister
public enum BugLevel implements EnumMessage<String> {

  CRITICAL, MAJOR, MINOR, TRIVIAL;

  public static final BugLevel DEFAULT = BugLevel.MINOR;

  @Override
  public String getValue() {
    return this.name();
  }

  public static BugLevel ofMessage(String massage, Locale locale) {
    for (BugLevel value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(massage.trim())) {
        return value;
      }
    }
    throw new CommProtocolException("Unknown bug level: " + massage);
  }
}
