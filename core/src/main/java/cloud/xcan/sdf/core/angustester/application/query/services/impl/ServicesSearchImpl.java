package cloud.xcan.sdf.core.angustester.application.query.services.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesSearch;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
  public Page<Services> search(Set<SearchCriteria> criterias, Pageable pageable, String... matches) {
    return new BizTemplate<Page<Services>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(criterias);
      }

      @Override
      protected Page<Services> process() {
        criterias.add(SearchCriteria.equal("deletedFlag", 0));
        commonQuery.checkAndSetAuthObjectIdCriteria(criterias);
        Page<Services> page = servicesSearchRepo.find(criterias, pageable, Services.class, matches);
        servicesQuery.setApisNum(page.getContent(), criterias);
        return page;
      }
    }.execute();
  }

}




