package cloud.xcan.angus.core.tester.interfaces.project.facade.vo;


import cloud.xcan.angus.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ProjectDetailVo {

  private Long id;

  private ProjectType type;

  private String avatar;

  private String name;

  private Long ownerId;

  @NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private String description;

  private LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> members;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedName;

  private LocalDateTime lastModifiedDate;

}



