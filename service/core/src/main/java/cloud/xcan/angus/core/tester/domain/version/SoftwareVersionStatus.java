package cloud.xcan.angus.core.tester.domain.version;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;

@EndpointRegister
public enum SoftwareVersionStatus implements EnumMessage<String> {

  NOT_RELEASED, RELEASED, ARCHIVED;

  public static final SoftwareVersionStatus DEFAULT = SoftwareVersionStatus.NOT_RELEASED;

  public boolean isReleased() {
    return this.equals(SoftwareVersionStatus.RELEASED);
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public static SoftwareVersionStatus ofMessage(String massage, Locale locale) {
    for (SoftwareVersionStatus value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(massage.trim())) {
        return value;
      }
    }
    throw new ProtocolException("Unknown version status: " + massage);
  }

}
