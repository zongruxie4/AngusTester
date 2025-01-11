package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintFindDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", example = "1", required = true)
  private Long projectId;

  private String name;

  private TaskSprintStatus status;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Long ownerId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}



