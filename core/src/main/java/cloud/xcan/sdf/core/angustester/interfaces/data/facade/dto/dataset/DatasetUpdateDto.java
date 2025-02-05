package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.dataset;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.model.element.dataset.DatasetParameter;
import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
public class DatasetUpdateDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long id;

  //@NotNull
  //@ApiModelProperty(value = "Project id", required = true)
  //private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Dataset name")
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Dataset description")
  private String description;

  @Size(max = MAX_PARAM_SIZE)
  @ApiModelProperty(value = "Parameters definition. ")
  private List<DatasetParameter> parameters;

  @Valid
  @ApiModelProperty(value = "Extraction rules configuration")
  private DefaultExtraction extraction;

}
