package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class VariableReplaceDto {

  @Schema(description="Modify variable id. Create a new variable when the value is null")
  private Long id;

  @Schema(description = "Project id, required when creating a new dataset")
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Variable name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Variable description")
  private String description;

  @Schema(description = "Required when non extraction type variable, the value of extraction type will be automatically set after extraction.")
  @Length(max = MAX_PARAM_VALUE_LENGTH)
  private String value;

  @NotNull
  @Schema(description = "Is the password type value, default `false`", requiredMode = RequiredMode.REQUIRED)
  private Boolean passwordValue;

  @Valid
  @Schema(description = "Extraction rules configuration")
  private DefaultExtraction extraction;

}
