package cloud.xcan.angus.core.tester.application.query.apis.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseSearch;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
  public Page<ApisCaseInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisCaseInfo> clz, String... matches) {
    return new BizTemplate<Page<ApisCaseInfo>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<ApisCaseInfo> process() {
        criteria.add(SearchCriteria.equal("apisDeleted", false));
        Page<ApisCaseInfo> page = apisCaseInfoSearchRepo.find(criteria, pageable, clz, matches);
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
