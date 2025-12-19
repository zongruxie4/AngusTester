package cloud.xcan.angus.core.tester.interfaces.test.facade.vo.baseline;

import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineDetailVo {

  private Long id;

  private String name;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Boolean established;

  private String description;

  private LinkedHashSet<Long> caseIds;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

  private Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;

}
