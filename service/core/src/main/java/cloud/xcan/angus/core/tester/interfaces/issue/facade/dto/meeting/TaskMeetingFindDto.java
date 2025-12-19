package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


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

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}



