package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareScope;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareSearchDto extends PageQuery {

  @Schema(description = "Share id")
  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Share name")
  private String name;

  @Schema(description = "Share expired date")
  private LocalDateTime expiredDate;

  @Schema(description = "Share scope")
  private ApisShareScope shareScope;

  @Schema(description = "Share services id")
  private Long servicesId;

  @Schema(description = "Share user id")
  private Long createdBy;

  @Schema(description = "Share date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
