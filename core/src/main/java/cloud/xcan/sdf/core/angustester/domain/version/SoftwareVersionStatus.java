package cloud.xcan.sdf.core.angustester.domain.version;

import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.locale.MessageHolder;
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
    throw new CommProtocolException("Unknown version status: " + massage);
  }

}
