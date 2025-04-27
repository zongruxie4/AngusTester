package cloud.xcan.angus.core.tester.domain.script;

import cloud.xcan.angus.spec.locale.EnumValueMessage;
import lombok.Getter;


@Getter
public enum ScriptFormat implements EnumValueMessage<String> {

  JSON(".json"), YAML(".yaml");

  private String fileSuffix;

  ScriptFormat(String fileSuffix) {
    this.fileSuffix = fileSuffix;
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isYaml() {
    return this.equals(YAML);
  }
}
