package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_DATASET_REVIEW_ROWS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class DatasetValuePreviewDto {

  @ApiModelProperty(value = "Dataset id")
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Variable name")
  private String name;

  @Size(max = MAX_PARAM_SIZE)
  @ApiModelProperty(value = "Parameters definition")
  private List<DatasetParameter> parameters;

  @Valid
  @ApiModelProperty(value = "Extraction rules configuration")
  private DefaultExtraction extraction;

  @Max(value = MAX_DATASET_REVIEW_ROWS)
  @ApiParam(name = "rowNum", value = "Preview rows")
  private Long rowNum;
}
