package cloud.xcan.angus.core.tester.domain.config.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.spec.utils.JsonUtils;
import jakarta.persistence.AttributeConverter;

public class PerfDataConverter implements AttributeConverter<PerfData, String> {

  @Override
  public String convertToDatabaseColumn(PerfData attribute) {
    try {
      return JsonUtils.toJson(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public PerfData convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return JsonUtils.fromJson(dbData, PerfData.class);
    } catch (Exception e) {
      return null;
    }
  }
}
