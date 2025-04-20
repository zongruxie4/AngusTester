package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author XiaoLong Liu
 */
@Setter
@Getter
@Accessors(chain = true)
public class TaskRemarkFindDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long taskId;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }


}
