package cloud.xcan.angus.core.tester.domain.config.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.config.indicator.FuncData;
import cloud.xcan.angus.spec.utils.JsonUtils;
import jakarta.persistence.AttributeConverter;

public class FuncDataConverter implements AttributeConverter<FuncData, String> {

  @Override
  public String convertToDatabaseColumn(FuncData attribute) {
    try {
      return JsonUtils.toJson(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public FuncData convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return JsonUtils.fromJson(dbData, FuncData.class);
    } catch (Exception e) {
      return null;
    }
  }
}
