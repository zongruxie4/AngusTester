package cloud.xcan.angus.core.tester.domain.task;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;


@EndpointRegister
public enum TaskType implements ValueObject<TaskType>, EnumMessage<String> {
  REQUIREMENT,
  STORY,
  TASK,
  BUG,
  API_TEST,
  SCENARIO_TEST;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isBug(){
    return this.equals(BUG);
  }

  public boolean isTestTask() {
    return this.equals(API_TEST) || this.equals(SCENARIO_TEST);
  }

  public boolean isApiTest() {
    return this.equals(API_TEST);
  }

  public boolean isScenarioTest() {
    return this.equals(SCENARIO_TEST);
  }

  public static TaskType ofMessage(String massage, Locale locale) {
    for (TaskType value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(massage.trim())) {
        return value;
      }
    }
    throw new ProtocolException("Unknown task type: " + massage);
  }
}
