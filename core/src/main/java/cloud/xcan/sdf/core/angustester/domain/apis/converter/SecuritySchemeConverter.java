package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.security.SecurityScheme;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SecuritySchemeConverter implements AttributeConverter<SecurityScheme, String> {

  @Override
  public String convertToDatabaseColumn(SecurityScheme attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public SecurityScheme convertToEntityAttribute(String dbData) {
    if (ObjectUtils.isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, SecurityScheme.class);
    } catch (Exception e) {
      return null;
    }
  }
}
