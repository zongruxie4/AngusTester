package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Plan id")
  private Long planId;

  private String name;

  private FuncPlanStatus status;

  private Long ownerId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

}
