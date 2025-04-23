package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineUpdateDto {

  @Schema(description="It is required to add a baseline.")
  private Long id;

  @Schema(description = "Baseline name, Brief overview of the baseline, supporting up to 200 characters.")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  //@Schema(description = "Plan id")
  //private Long planId;

  @EditorContentLength
  @Schema(description = "Baseline information")
  private String description;

  //@Size(max = MAX_OPT_CASE_NUM)
  //@Schema(description="It is valid to add a baseline.")
  //private LinkedHashSet<Long> caseIds;

}
