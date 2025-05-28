package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class DatasetReplaceDto {

  @Schema(description="Modify dataset id. Create a new dataset when the value is null")
  private Long id;

  @Schema(description = "Project id, required when creating a new dataset")
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Dataset name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Dataset description")
  private String description;

  @NotEmpty
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Parameters definition. ", requiredMode = RequiredMode.REQUIRED)
  private List<DatasetParameter> parameters;

  @Valid
  @Schema(description = "Extraction rules configuration")
  private DefaultExtraction extraction;
}
