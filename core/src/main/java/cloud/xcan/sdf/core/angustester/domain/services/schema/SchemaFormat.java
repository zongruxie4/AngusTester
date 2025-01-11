package cloud.xcan.sdf.core.angustester.domain.services.schema;

public enum SchemaFormat {
  yaml, json;

  public boolean isYaml() {
    return this.equals(yaml);
  }
}
