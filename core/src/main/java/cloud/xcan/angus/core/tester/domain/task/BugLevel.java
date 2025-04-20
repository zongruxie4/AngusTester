package cloud.xcan.angus.core.tester.domain.task;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
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
    throw new ProtocolException("Unknown bug level: " + massage);
  }
}
