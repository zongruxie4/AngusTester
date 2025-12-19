package cloud.xcan.angus.core.tester.interfaces.project.facade.vo;

import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class TagVo {

  private Long id;

  private String name;

  private Long projectId;

  private boolean hasEditPermission = false;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

}
