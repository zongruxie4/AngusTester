package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class DatasetFindDto extends PageQuery {

  @Schema(description = "Dataset identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for dataset query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Dataset name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Creator identifier for dataset ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Dataset creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for dataset update tracking")
  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Dataset last modification date for temporal filtering")
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



