package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class TaskMeetingFindDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Meeting sprint id")
  private Long sprintId;

  @Schema(description = "Meeting moderator id")
  private Long moderatorId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Meeting subject")
  private String subject;

  @Schema(description = "Meeting type, Specify the type of meeting for identification and management purposes")
  private TaskMeetingType type;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}



