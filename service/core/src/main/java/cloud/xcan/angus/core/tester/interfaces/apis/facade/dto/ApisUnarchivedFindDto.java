package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
public class ApisUnarchivedFindDto extends PageQuery {

  @Schema(description = "Unarchived API identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for unarchived API query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "API endpoint path for filtering and search")
  private String endpoint;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  @Schema(description = "API summary or name for filtering and search")
  private String summary;

  @Length(max = MAX_CODE_LENGTH_X5)
  @Schema(description = "Operation identifier for filtering and search")
  private String operationId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Creator identifier for ownership filtering")
  private Long createdBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
