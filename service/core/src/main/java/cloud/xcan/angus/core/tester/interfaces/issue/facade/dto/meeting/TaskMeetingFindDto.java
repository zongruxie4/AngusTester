package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingType;
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

  @Schema(description = "Meeting identifier for specific meeting lookup")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for meeting filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint identifier for meeting iteration filtering")
  private Long sprintId;

  @Schema(description = "Meeting moderator identifier for host filtering")
  private Long moderatorId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Meeting subject for partial matching search")
  private String subject;

  @Schema(description = "Meeting type classification for workflow filtering")
  private TaskMeetingType type;

  @Schema(description = "Meeting creator identifier for ownership filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Meeting creation timestamp for timeline filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for change tracking")
  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}



