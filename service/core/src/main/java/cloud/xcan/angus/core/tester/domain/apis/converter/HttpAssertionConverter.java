package cloud.xcan.angus.core.tester.domain.apis.converter;

import static cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class HttpAssertionConverter implements
    AttributeConverter<List<Assertion<HttpExtraction>>, String> {

  @Override
  public String convertToDatabaseColumn(List<Assertion<HttpExtraction>> attribute) {
    try {
      return OPENAPI_MAPPER.writeValueAsString(attribute);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<Assertion<HttpExtraction>> convertToEntityAttribute(String dbData) {
    if (isEmpty(dbData)) {
      return null;
    }
    try {
      return OPENAPI_MAPPER.readValue(dbData, new TypeReference<List<Assertion<HttpExtraction>>>() {
      });
    } catch (Exception e) {
      return null;
    }
  }
}
