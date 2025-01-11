package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SecurityRequirementConverter implements
    AttributeConverter<List<SecurityRequirement>, String> {

  @Override
  public String convertToDatabaseColumn(List<SecurityRequirement> attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<SecurityRequirement> convertToEntityAttribute(String dbData) {
    if (ObjectUtils.isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, new TypeReference<List<SecurityRequirement>>() {
      });
    } catch (Exception e) {
      return null;
    }
  }
}
