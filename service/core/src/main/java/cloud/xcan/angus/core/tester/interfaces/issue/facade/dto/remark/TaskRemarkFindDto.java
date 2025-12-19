package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.remark;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskRemarkFindDto extends PageQuery {

  @Schema(description = "Remark identifier for specific remark lookup")
  private Long id;

  @NotNull
  @Schema(description = "Task identifier for remark filtering and association", requiredMode = RequiredMode.REQUIRED)
  private Long taskId;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
