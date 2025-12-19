package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.share;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareScope;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ApisShareFindDto extends PageQuery {

  @Schema(description = "Share identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for share query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Share name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Share expiration date for temporal filtering")
  private LocalDateTime expiredDate;

  @Schema(description = "Share scope for access control filtering")
  private ApisShareScope shareScope;

  @Schema(description = "Service identifier for share association filtering")
  private Long servicesId;

}
