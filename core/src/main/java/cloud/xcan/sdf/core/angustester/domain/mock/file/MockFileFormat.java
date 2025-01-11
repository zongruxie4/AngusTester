package cloud.xcan.sdf.core.angustester.domain.mock.file;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum MockFileFormat implements EnumValueMessage<String> {
  CSV, XML, JSON, SQL, EXCEL, TXT, BINARY;

  @Override
  public String getValue() {
    return this.name();
  }
}
