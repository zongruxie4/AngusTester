package cloud.xcan.angus.core.tester.application.query.data.impl;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.data.DatasourceSearch;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class DatasourceSearchImpl implements DatasourceSearch {

  @Resource
  private DatasourceSearchRepo mockDatasourceSearchRepo;

  @Resource
  private UserManager userManager;

  @Override
  public Page<Datasource> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Datasource> clz, String... matches) {
    return new BizTemplate<Page<Datasource>>() {

      @Override
      protected Page<Datasource> process() {
        Page<Datasource> page = mockDatasourceSearchRepo
            .find(criteria, pageable, clz, matches);
        if (page.hasContent()) {
          userManager.setUserNameAndAvatar(page.getContent(), "lastModifiedBy",
              "lastModifiedByName", "avatar");
        }
        return page;
      }
    }.execute();
  }

}




