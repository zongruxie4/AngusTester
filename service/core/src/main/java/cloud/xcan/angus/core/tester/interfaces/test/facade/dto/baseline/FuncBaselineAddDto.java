package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
  @Schema(description = "Functional test baseline name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @NotNull
  @Schema(description = "Functional test plan identifier for baseline association", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @EditorContentLength
  @Schema(description = "Comprehensive baseline description and documentation")
  private String description;

  @Size(max = MAX_OPT_CASE_NUM)
  @Schema(description = "Test case identifiers for baseline versioning and tracking")
  private LinkedHashSet<Long> caseIds;

}
