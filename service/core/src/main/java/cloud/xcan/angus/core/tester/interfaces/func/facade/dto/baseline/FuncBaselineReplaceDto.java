package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncBaselineReplaceDto {

  @Schema(description="It is required to add a baseline")
  private Long id;

  @NotBlank
  @Schema(description = "Baseline name, Brief overview of the baseline, supporting up to 200 characters", example = "Example Baseline", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @Schema(description = "Plan id. It is required to add a baseline")
  private Long planId;

  @EditorContentLength
  @Schema(description = "Baseline information")
  private String description;

  @Size(max = MAX_OPT_CASE_NUM)
  @Schema(description="It is valid to add a baseline")
  private LinkedHashSet<Long> caseIds;

}
