package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncBaselineUpdateDto {

  @Schema(description = "Functional test baseline identifier for update operation")
  private Long id;

  @Schema(description = "Functional test baseline name for identification and organization")
  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  //@Schema(description = "Plan id")
  //private Long planId;

  @EditorContentLength
  @Schema(description = "Comprehensive baseline description and documentation")
  private String description;

  //@Size(max = MAX_OPT_CASE_NUM)
  //@Schema(description="It is valid to add a baseline")
  //private LinkedHashSet<Long> caseIds;

}
