package cloud.xcan.sdf.core.angustester.interfaces.analysis.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class TaskCreatorStatisticsDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  private Long sprintId;

  @ApiModelProperty(value = "Creator object type, Default USER", example = "USER")
  private AuthObjectType creatorObjectType;

  @ApiModelProperty(value = "Creator object id, Default current user id")
  private Long creatorObjectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Resources creation end date")
  private LocalDateTime createdDateEnd;

}
