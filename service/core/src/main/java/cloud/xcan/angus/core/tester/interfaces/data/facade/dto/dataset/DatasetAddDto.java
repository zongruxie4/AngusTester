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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class DatasetAddDto {

  @NotNull
  @Schema(description = "Project identifier for dataset association", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Dataset name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Dataset description for detailed documentation")
  private String description;

  @NotEmpty
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Dataset parameter definitions with comprehensive configuration options", requiredMode = RequiredMode.REQUIRED)
  private List<DatasetParameter> parameters;

  @Valid
  @Schema(description = "Data extraction rules configuration for automated data processing")
  private DefaultExtraction extraction;

}
