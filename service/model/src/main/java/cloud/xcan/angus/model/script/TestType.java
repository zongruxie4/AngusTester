package cloud.xcan.angus.model.script;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;

@EndpointRegister
public enum TestType implements EnumMessage<String> {
  PERFORMANCE, FUNCTIONAL, STABILITY, CUSTOMIZATION;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPerformance() {
    return this.equals(PERFORMANCE);
  }

  public boolean isFunctional() {
    return this.equals(FUNCTIONAL);
  }

  public boolean isStability() {
    return this.equals(STABILITY);
  }

  public static TestType of(ScriptType scriptType) {
    switch (scriptType) {
      case TEST_PERFORMANCE:
        return TestType.PERFORMANCE;
      case TEST_FUNCTIONALITY:
        return TestType.FUNCTIONAL;
      case TEST_STABILITY:
        return TestType.STABILITY;
    }
    return TestType.CUSTOMIZATION;
  }

  public ScriptType toScriptType() {
    if (this.isFunctional()) {
      return ScriptType.TEST_FUNCTIONALITY;
    } else if (this.isStability()) {
      return ScriptType.TEST_STABILITY;
    } else if (this.isPerformance()) {
      return ScriptType.TEST_PERFORMANCE;
    } else {
      return ScriptType.TEST_CUSTOMIZATION;
    }
  }

  public static TestType ofMessage(String taskTypeMassage, Locale locale) {
    for (TestType value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(taskTypeMassage.trim())) {
        return value;
      }
    }
    throw new ProtocolException("Unknown test type: " + taskTypeMassage);
  }
}
