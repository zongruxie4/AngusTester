package cloud.xcan.sdf.core.angustester.domain.apis.converter;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.info.Info;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class InfoConverter implements AttributeConverter<Info, String> {

  @Override
  public String convertToDatabaseColumn(Info attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Info convertToEntityAttribute(String dbData) {
    if (ObjectUtils.isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, Info.class);
    } catch (Exception e) {
      return null;
    }
  }
}
