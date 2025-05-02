package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.List;
import java.util.Set;

public interface ProjectMemberQuery {

  Set<Long> findMemberUserIds(Long projectId);

  Set<Long> getMemberIds(AuthObjectType creatorObjectType,
      Long creatorObjectId, Long projectId);

  void checkMember(Set<SearchCriteria> criteria);

  void checkMember(Long userId, Long projectId);

  void setMembers(Project project);

  void setMembers(List<Project> projects);
}
