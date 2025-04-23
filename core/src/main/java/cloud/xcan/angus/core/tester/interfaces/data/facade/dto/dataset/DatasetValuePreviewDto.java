package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_DATASET_REVIEW_ROWS;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class DatasetValuePreviewDto {

  @Schema(description = "Dataset id")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Variable name")
  private String name;

  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Parameters definition")
  private List<DatasetParameter> parameters;

  @Valid
  @Schema(description = "Extraction rules configuration")
  private DefaultExtraction extraction;

  @Max(value = MAX_DATASET_REVIEW_ROWS)
  @Schema(name = "rowNum", description = "Preview rows")
  private Long rowNum;
}
