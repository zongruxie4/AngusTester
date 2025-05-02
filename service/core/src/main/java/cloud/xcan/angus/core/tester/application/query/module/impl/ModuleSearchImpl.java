package cloud.xcan.angus.core.tester.application.query.module.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.module.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.module.ModuleSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.domain.module.ModuleSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;
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
  public List<Module> search(Set<SearchCriteria> criteria, Class<Module> clz, String... matches) {
    return new BizTemplate<List<Module>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected List<Module> process() {
        List<Module> modules = moduleSearchRepo.find(criteria,
            PageRequest.of(0, 10000, JpaSort.by(Order.asc("id"))), clz, matches).getContent();
        List<Module> allModules = moduleQuery.findAndAllParent(modules);
        moduleQuery.setEditPermission(criteria, allModules);
        return allModules;
      }
    }.execute();
  }
}
