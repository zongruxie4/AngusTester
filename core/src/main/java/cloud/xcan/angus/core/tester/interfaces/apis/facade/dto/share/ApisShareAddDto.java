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

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareAddDto {

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Share name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Share remark, it will be displayed on the sharing page to information")
  private String remark;

  @Schema(description = "Share expired date")
  private LocalDateTime expiredDate;

  @Valid
  @NotNull
  @Schema(description = "Share display options", requiredMode = RequiredMode.REQUIRED)
  private DisplayOptions displayOptions;

  @NotNull
  @Schema(description = "Share scope", requiredMode = RequiredMode.REQUIRED)
  private ApisShareScope shareScope;

  @NotNull
  @Schema(description = "Share services id", requiredMode = RequiredMode.REQUIRED)
  private Long servicesId;

  @Schema(description = "Share apis ids, it is required when share scope is apis")
  private Set<Long> apisIds;

  @NotEmpty
  @Length(max = MAX_URL_LENGTH)
  @Schema(description = "Web front end sharing page address", requiredMode = RequiredMode.REQUIRED)
  private String baseUrl;

}
