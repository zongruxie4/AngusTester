package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.remark;

import cloud.xcan.angus.core.tester.domain.task.TaskType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskRemarkDetailVo {

  private Long id;

  private Long taskId;

  private Long remarkId;

  private TaskType taskType;

  private String content;

  private Long createdBy;

  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;
}
