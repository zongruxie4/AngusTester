package cloud.xcan.angus.core.tester.domain.config.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.config.indicator.StabilityData;
import cloud.xcan.angus.spec.utils.JsonUtils;
import jakarta.persistence.AttributeConverter;

public class StabilityDataConverter implements AttributeConverter<StabilityData, String> {

  @Override
  public String convertToDatabaseColumn(StabilityData attribute) {
    try {
      return JsonUtils.toJson(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public StabilityData convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return JsonUtils.fromJson(dbData, StabilityData.class);
    } catch (Exception e) {
      return null;
    }
  }
}
