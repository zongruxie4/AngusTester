package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class VariableValuePreviewDto {

  @Schema(description = "Variable id")
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Variable name")
  private String name;

  @Length(max = MAX_PARAM_VALUE_LENGTH)
  @Schema(description = "Required when non extraction type variable, the value of extraction type will be automatically set after extraction.")
  private String value;

  @Valid
  @Schema(description = "Extraction rules configuration")
  private DefaultExtraction extraction;

}
