package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class VariableAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Variable name", required = true)
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Variable description")
  private String description;

  @ApiModelProperty(value = "Required when non extraction type variable, the value of extraction type will be automatically set after extraction.")
  @Length(max = MAX_PARAM_VALUE_LENGTH)
  private String value;

  @NotNull
  @ApiModelProperty(value = "Is the password type value, default `false`", required = true)
  private Boolean passwordValue;

  @Valid
  @ApiModelProperty(value = "Extraction rules configuration")
  private DefaultExtraction extraction;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;
}
