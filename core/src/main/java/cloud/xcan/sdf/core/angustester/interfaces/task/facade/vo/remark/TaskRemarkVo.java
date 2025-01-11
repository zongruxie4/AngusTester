package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.remark;

import cloud.xcan.sdf.api.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskRemarkVo {

  private Long id;

  private String content;

  private Long taskId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

}
