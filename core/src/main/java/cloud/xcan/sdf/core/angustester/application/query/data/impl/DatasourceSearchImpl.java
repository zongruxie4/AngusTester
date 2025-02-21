package cloud.xcan.sdf.core.angustester.application.query.data.impl;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasourceSearch;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.DatasourceSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
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
      protected void checkParams() {
        // NOOP
      }

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




