package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.sdf.spec.locale.EnumValueMessage;
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
