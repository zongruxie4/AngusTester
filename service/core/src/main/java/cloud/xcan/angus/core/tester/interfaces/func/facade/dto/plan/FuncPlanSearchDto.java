package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private String name;

  //Note: All project members are visible
  //@Schema(description = "Required when app administrators query all task")
  //private Boolean admin;

  private Boolean review;

  private FuncPlanStatus status;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Long ownerId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

}
