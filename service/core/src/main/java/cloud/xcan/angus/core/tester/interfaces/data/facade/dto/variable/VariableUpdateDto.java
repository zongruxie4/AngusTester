package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class VariableUpdateDto {

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@NotNull
  //@Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  //private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Variable name")
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Variable description")
  private String description;

  @Length(max = MAX_PARAM_VALUE_LENGTH)
  @Schema(description = "Required when non extraction type variable, the value of extraction type will be automatically set after extraction.")
  private String value;

  @Schema(description = "Is the password type value, default `false`")
  private Boolean passwordValue;

  @Valid
  @Schema(description = "Extraction rules configuration")
  private DefaultExtraction extraction;

}
