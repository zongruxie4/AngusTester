package cloud.xcan.angus.core.tester.interfaces.project.facade.vo;

import cloud.xcan.angus.core.tester.domain.issue.count.ProgressCount;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersionStatus;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class SoftwareVersionVo {

  private Long id;

  private Long projectId;

  private String name;

  private LocalDateTime startDate;

  private LocalDateTime releaseDate;

  private SoftwareVersionStatus status;

  private String description;

  private ProgressCount progress;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
