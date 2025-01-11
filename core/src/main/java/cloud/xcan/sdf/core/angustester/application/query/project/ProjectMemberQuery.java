package cloud.xcan.sdf.core.angustester.application.query.project;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import java.util.List;
import java.util.Set;

public interface ProjectMemberQuery {

  Set<Long> findMemberUserIds(Long projectId);

  Set<Long> getMemberIds(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId);

  void checkMember(Set<SearchCriteria> criteria);

  void checkMember(Long projectId, Long userId);

  void setMembers(Project project);

  void setMembers(List<Project> projects);
}
