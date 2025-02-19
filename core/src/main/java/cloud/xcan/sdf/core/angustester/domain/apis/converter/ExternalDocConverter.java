package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.ExternalDocumentation;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ExternalDocConverter implements AttributeConverter<ExternalDocumentation, String> {

  @Override
  public String convertToDatabaseColumn(ExternalDocumentation attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public ExternalDocumentation convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, ExternalDocumentation.class);
    } catch (Exception e) {
      return null;
    }
  }
}
