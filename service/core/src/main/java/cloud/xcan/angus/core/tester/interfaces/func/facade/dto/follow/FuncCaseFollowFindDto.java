package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseFollowFindDto extends PageQuery {

  @Schema(description = "Project identifier for followed case scope filtering")
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Followed test case name for fuzzy search and filtering")
  private String caseName;

}



