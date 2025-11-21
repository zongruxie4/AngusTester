package cloud.xcan.angus.core.tester.interfaces.config.facade.dto.dns;

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
public class NodeDomainDnsFindDto extends PageQuery {

  @Schema(description = "DNS record identifier for precise query filtering")
  private Long id;

  @Schema(description = "Domain identifier for DNS record scope filtering")
  private Long domainId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "DNS record name for fuzzy search or filtering")
  private String name;

  @Schema(description = "DNS record status for active/inactive filtering")
  private NormalStatus status;

  @Schema(description = "Creator identifier for ownership-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "DNS record creation date for temporal filtering")
  private LocalDateTime createdDate;
}



