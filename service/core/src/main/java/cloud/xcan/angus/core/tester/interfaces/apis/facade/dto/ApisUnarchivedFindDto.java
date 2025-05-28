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

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_URL_LENGTH_X4)
  private String endpoint;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary;

  @Length(max = MAX_CODE_LENGTH_X5)
  private String operationId;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
