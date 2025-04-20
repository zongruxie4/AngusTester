package cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline;

import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineVo {

  private Long id;

  private String name;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Boolean established;

  private String description;

  private Long createdBy;

  //@NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private String createdByAvatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
