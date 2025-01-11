package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases;

import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_OPT_CASE_NUM;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCaseAddDto {

  @NotNull
  @ApiModelProperty(value = "Review id", example = "1", required = true)
  private Long reviewId;

  @NotEmpty
  @Size(max = MAX_OPT_CASE_NUM)
  @ApiModelProperty(value = "Review case ids", required = true)
  private LinkedHashSet<Long> caseIds;

}
