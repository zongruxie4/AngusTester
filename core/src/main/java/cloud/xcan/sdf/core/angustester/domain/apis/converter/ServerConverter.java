package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ServerConverter implements AttributeConverter<Server, String> {

  @Override
  public String convertToDatabaseColumn(Server attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Server convertToEntityAttribute(String dbData) {
    if (ObjectUtils.isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, Server.class);
    } catch (Exception e) {
      return null;
    }
  }
}
