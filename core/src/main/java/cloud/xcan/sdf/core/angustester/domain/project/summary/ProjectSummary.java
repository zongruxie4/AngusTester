package cloud.xcan.sdf.core.angustester.domain.project.summary;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ProjectSummary {

  private Long id;

  private String name;

  private Long ownerId;

  //@NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private String ownerAvatar;

  private String startDate;

  private String deadlineDate;

  private String description;

  private LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> members;

}
