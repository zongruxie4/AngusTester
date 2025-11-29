package cloud.xcan.angus.core.tester.domain.config.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.config.tenant.apiproxy.ServerApiProxy;
import cloud.xcan.angus.core.utils.GsonUtils;
import jakarta.persistence.AttributeConverter;

public class ServerApiProxyDataConverter implements AttributeConverter<ServerApiProxy, String> {

  @Override
  public String convertToDatabaseColumn(ServerApiProxy attribute) {
    try {
      return GsonUtils.toJson(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ServerApiProxy convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return GsonUtils.fromJson(dbData, ServerApiProxy.class);
    } catch (Exception e) {
      return null;
    }
  }
}
