package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.follow;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.remote.PageQuery;
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
public class FuncCaseFollowSearchDto extends PageQuery {

  @Schema(description = "Project id")
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Follow case name")
  private String caseName;

}



