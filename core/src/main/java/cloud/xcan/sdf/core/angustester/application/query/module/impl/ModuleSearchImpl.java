package cloud.xcan.sdf.core.angustester.application.query.module.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleQuery;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.angustester.domain.module.ModuleSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.JpaSort;

@Biz
public class ModuleSearchImpl implements ModuleSearch {

  @Resource
  private ModuleSearchRepo moduleSearchRepo;

  @Resource
  private ModuleQuery moduleQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public List<Module> search(Set<SearchCriteria> criterias, Class<Module> clz, String... matches) {
    return new BizTemplate<List<Module>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected List<Module> process() {
        List<Module> modules = moduleSearchRepo.find(criterias,
            PageRequest.of(0, 10000, JpaSort.by(Order.asc("id"))), clz, matches).getContent();
        List<Module> allModules = moduleQuery.findAndAllParent(modules);
        moduleQuery.setEditPermissionFlag(criterias, allModules);
        return allModules;
      }
    }.execute();
  }
}
