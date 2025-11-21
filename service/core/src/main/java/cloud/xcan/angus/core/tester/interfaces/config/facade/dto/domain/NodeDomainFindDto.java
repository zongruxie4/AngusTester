package cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
public class NodeDomainFindDto extends PageQuery {

  @Schema(description = "Domain identifier for precise query filtering")
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Domain name for fuzzy search or filtering")
  private String name;

  @Schema(description = "Domain status for active/inactive filtering")
  private NormalStatus status;

  @Schema(description = "Creator identifier for ownership-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Domain creation date for temporal filtering")
  private LocalDateTime createdDate;

}



