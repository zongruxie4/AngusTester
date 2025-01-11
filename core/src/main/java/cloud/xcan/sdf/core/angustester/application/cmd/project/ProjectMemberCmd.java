package cloud.xcan.sdf.core.angustester.application.cmd.project;

import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public interface ProjectMemberCmd {

  void add0(Long projectId, LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds);

  void replace0(Long projectId, LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds);
}
