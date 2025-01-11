package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisCaseSearch;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfoSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class ApisCaseSearchImpl implements ApisCaseSearch {

  @Resource
  private ApisCaseInfoSearchRepo apisCaseInfoSearchRepo;

  @Resource
  private ApisCaseQuery funcCaseQuery;

  @Resource
  private UserManager userManager;

  @Override
  public Page<ApisCaseInfo> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ApisCaseInfo> clz, String... matches) {
    return new BizTemplate<Page<ApisCaseInfo>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ApisCaseInfo> process() {
        criterias.add(SearchCriteria.equal("apisDeletedFlag", false));
        Page<ApisCaseInfo> page = apisCaseInfoSearchRepo.find(criterias, pageable, clz, matches);
        if (page.hasContent()) {
          // Set apis deleted flag
          funcCaseQuery.setInfoApisNameAndDeleted(page.getContent());
          // Set user name and avatar
          userManager.setUserNameAndAvatar(page.getContent(), "createdBy");
        }
        return page;
      }
    }.execute();
  }

}
