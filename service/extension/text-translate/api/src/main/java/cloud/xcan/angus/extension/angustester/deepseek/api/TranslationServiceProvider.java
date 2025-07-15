package cloud.xcan.angus.extension.angustester.deepseek.api;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum TranslationServiceProvider implements EnumValueMessage<String> {
  Aliyun, DeepSeek;

  @Override
  public String getValue() {
    return this.name();
  }

}

