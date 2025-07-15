package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncBaselineAddDto {

  @NotBlank
  @Schema(description = "Baseline name, Brief overview of the baseline, supporting up to 200 characters")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @NotNull
  @Schema(description = "Plan id")
  private Long planId;

  @EditorContentLength
  @Schema(description = "Baseline information")
  private String description;

  @Size(max = MAX_OPT_CASE_NUM)
  private LinkedHashSet<Long> caseIds;

}
