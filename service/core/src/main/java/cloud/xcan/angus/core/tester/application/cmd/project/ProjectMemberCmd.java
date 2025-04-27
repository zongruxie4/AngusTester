package cloud.xcan.angus.core.tester.application.cmd.project;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public interface ProjectMemberCmd {

  void add0(Long projectId, LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds);

  void replace0(Long projectId, LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds);
}
