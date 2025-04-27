package cloud.xcan.angus.core.tester.domain.services.schema;

public enum SchemaFormat {
  yaml, json;

  public boolean isYaml() {
    return this.equals(yaml);
  }
}
