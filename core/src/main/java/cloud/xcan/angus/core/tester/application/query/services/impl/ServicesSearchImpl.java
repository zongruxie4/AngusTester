package cloud.xcan.angus.core.tester.application.query.services.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSearch;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ServicesSearchImpl implements ServicesSearch {

  @Resource
  private ServicesSearchRepo servicesSearchRepo;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Page<Services> search(Set<SearchCriteria> criteria, Pageable pageable, String... matches) {
    return new BizTemplate<Page<Services>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<Services> process() {
        criteria.add(SearchCriteria.equal("deleted", 0));
        commonQuery.checkAndSetAuthObjectIdCriteria(criteria);
        Page<Services> page = servicesSearchRepo.find(criteria, pageable, Services.class, matches);
        servicesQuery.setApisNum(page.getContent(), criteria);
        return page;
      }
    }.execute();
  }

}




