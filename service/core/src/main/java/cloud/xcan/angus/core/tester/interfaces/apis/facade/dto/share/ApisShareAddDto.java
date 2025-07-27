package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareScope;
import cloud.xcan.angus.core.tester.domain.apis.share.DisplayOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareAddDto {

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Share name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Share remark for display on sharing page information")
  private String remark;

  @Schema(description = "Share expiration date for access control")
  private LocalDateTime expiredDate;

  @Valid
  @NotNull
  @Schema(description = "Share display options for presentation configuration", requiredMode = RequiredMode.REQUIRED)
  private DisplayOptions displayOptions;

  @NotNull
  @Schema(description = "Share scope for access control definition", requiredMode = RequiredMode.REQUIRED)
  private ApisShareScope shareScope;

  @NotNull
  @Schema(description = "Service identifier for share association", requiredMode = RequiredMode.REQUIRED)
  private Long servicesId;

  @Schema(description = "API identifiers for selective sharing when scope is APIs")
  private Set<Long> apisIds;

  @NotEmpty
  @Length(max = MAX_URL_LENGTH)
  @Schema(description = "Web frontend sharing page base URL for public access", requiredMode = RequiredMode.REQUIRED)
  private String baseUrl;

}
