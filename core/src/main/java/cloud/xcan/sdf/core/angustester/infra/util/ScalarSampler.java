package cloud.xcan.sdf.core.angustester.infra.util;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.util.SchemaTypeUtil;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @see SchemaTypeUtil
 */
class ScalarSampler {

  static final String STRING_DEFAULT = "string";
  static final OffsetDateTime DATE = OffsetDateTime.parse("2023-03-29T21:14:08Z");

  static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE.withZone(
      ZoneId.from(ZoneOffset.UTC));
  static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(
      ZoneId.from(ZoneOffset.UTC));

  public Object sample(Schema<?> schema) {
    if (schema.getEnum() != null) {
      return enumeration(schema);
    }
    if (schema.getType() == null) {
      return null;
    }
    switch (schema.getType()) {
      case "string":
        return string(schema);
      case "number":
        return number(schema);
      case "integer":
        return integer(schema);
      case "boolean":
        return bool(schema);
      default:
        return null;
    }
  }

  private Object enumeration(Schema<?> schema) {
    if (schema.getEnum().isEmpty()) {
      return null;
    } else {
      return schema.getEnum().get(0);
    }
  }

  private Object bool(Schema<?> schema) {
    return false;
  }

  private Object integer(Schema<?> schema) {
    if (schema.getMinimum() != null) {
      return schema.getMinimum();
    }
    return BigDecimal.ZERO;
  }

  private Object number(Schema<?> schema) {
    return 0.0;
  }

  private Object string(Schema<?> schema) {
    if (schema.getFormat() == null) {
      return STRING_DEFAULT;
    }
    switch (schema.getFormat()) {
      case "date":
        return ScalarSampler.DATE_FORMATTER.format(DATE);
      case "date-time":
        return ScalarSampler.DATE_TIME_FORMATTER.format(DATE);
      case "password":
        return "qwerty";
      case "byte":
      case "binary":
        return "aGVsbG8xCg==";
      case "email":
        return "info@example.com";
      case "uuid":
      case "guid":
        return UUID.randomUUID().toString();
      default:
        return STRING_DEFAULT;
    }
  }

}
