package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ServersConverter implements AttributeConverter<List<Server>, String> {

  @Override
  public String convertToDatabaseColumn(List<Server> attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<Server> convertToEntityAttribute(String dbData) {
    if (ObjectUtils.isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, new TypeReference<List<Server>>() {
      });
    } catch (Exception e) {
      return null;
    }
  }
}
