package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.mock;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisAssocMockApisAddDto {

  @NotNull
  @Schema(description = "Mock service identifier for API association", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "Mock API name for identification and organization", example = "This is mock apis name")
  private String summary;

}
