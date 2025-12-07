package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.tester.domain.project.member.ProjectMember;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

public class ProjectMemberConverter {

  public static List<ProjectMember> toDomains(Long projectId,
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> members) {
    List<ProjectMember> members0 = new ArrayList<>();
    for (Entry<OrgTargetType, LinkedHashSet<Long>> entry : members.entrySet()) {
      if (isEmpty(entry.getValue())) {
        continue;
      }
      for (Long id : entry.getValue()) {
        members0.add(new ProjectMember()
            .setProjectId(projectId).setMemberType(entry.getKey())
            .setMemberId(id));
      }
    }
    return members0;
  }

}
