package cloud.xcan.angus.core.tester.domain.mock.file;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum MockFileFormat implements EnumValueMessage<String> {
  CSV, XML, JSON, SQL, EXCEL, TXT, BINARY;

  @Override
  public String getValue() {
    return this.name();
  }
}
