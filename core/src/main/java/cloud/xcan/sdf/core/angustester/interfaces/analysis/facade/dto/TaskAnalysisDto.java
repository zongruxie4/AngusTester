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
public class TaskAnalysisDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Sprint id")
  private Long sprintId;

  @ApiModelProperty(value = "Analysis organization type")
  private AuthObjectType orgType;
  @ApiModelProperty(value = "Analysis organization id")
  private Long orgId;

  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Analysis data start time")
  private LocalDateTime startTime;
  @DateTimeFormat(pattern = DATE_FMT)
  @ApiModelProperty(value = "Analysis data end time")
  private LocalDateTime endTime;

  @ApiModelProperty(value = "Contains assignee analysis, default true")
  private boolean containsUserAnalysis = true;
  @ApiModelProperty(value = "Contains data details, default true")
  private boolean containsDataDetail = true;

}
