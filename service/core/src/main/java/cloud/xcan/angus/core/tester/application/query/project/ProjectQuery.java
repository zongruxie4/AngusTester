package cloud.xcan.angus.core.tester.application.query.project;

import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectQuery {

  List<Project> userJoined(Long userId, String name);

  List<UserBase> userMember(Long id);

  Project detail(Long id);

  Page<Project> find(GenericSpecification<Project> spec, Pageable pageable);

  boolean isAgile(Long id);

  Project checkAndFind(Long id);

  List<Project> find0ById(Set<Long> ids);

  void checkQuota(int inc);

  void checkAddNameExists(String name);

  void checkUpdateNameExists(Long id, String name);

  void checkModifyPermission(Project projectDb);

  void checkDeletePermission(Project projectDb);

  void checkEditTagPermission(Project projectDb);

  void checkEditModulePermission(Project projectDb);

  boolean hasEditPermission(Long projectId);

  boolean hasEditPermission(Project projectDb);

}




