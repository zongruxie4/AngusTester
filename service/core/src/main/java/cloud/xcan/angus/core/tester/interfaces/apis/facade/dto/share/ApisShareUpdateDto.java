package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareScope;
import cloud.xcan.angus.core.tester.domain.apis.share.DisplayOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
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
public class ApisShareUpdateDto {

  @NotNull
  @Schema(description = "Share identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Share name for identification and organization")
  private String name;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Share remark for display on sharing page information")
  private String remark;

  @Schema(description = "Share expiration date for access control")
  private LocalDateTime expiredDate;

  @Valid
  @Schema(description = "Share display options for presentation configuration")
  private DisplayOptions displayOptions;

  @Schema(description = "Share scope for access control definition")
  private ApisShareScope shareScope;

  @Schema(description = "Service identifier for share association")
  private Long servicesId;

  @Schema(description = "API identifiers for selective sharing when scope is APIs")
  private Set<Long> apisIds;

  @Length(max = MAX_URL_LENGTH)
  @Schema(description = "Web frontend sharing page base URL for public access")
  private String baseUrl;

}
