package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ApisDesignContentReplaceDto {

  @NotNull
  @Schema(description = "Design ID", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @NotNull
  @Schema(description = "Design content", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_OPENAPI_LENGTH)
  private String openapi;

}
