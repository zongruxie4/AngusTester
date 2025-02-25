package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisDesignQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisDesignSearch;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfoSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
