package cloud.xcan.angus.core.tester.application.query.apis.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisDesignSearch;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ApisDesignSearchImpl implements ApisDesignSearch {

  @Resource
  private ApisDesignInfoSearchRepo apisDesignInfoSearchRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ApisDesignQuery apisDesignQuery;

  @Override
  public Page<ApisDesignInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      String... matches) {
    return new BizTemplate<Page<ApisDesignInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project permission
        projectMemberQuery.checkMember(criteria);
      }

      @Override
      protected Page<ApisDesignInfo> process() {
        Page<ApisDesignInfo> page = apisDesignInfoSearchRepo.find(criteria, pageable,
            ApisDesignInfo.class, matches);
        if (page.hasContent()) {
          apisDesignQuery.setServicesName(page.getContent());
        }
        return page;
      }
    }.execute();
  }
}
