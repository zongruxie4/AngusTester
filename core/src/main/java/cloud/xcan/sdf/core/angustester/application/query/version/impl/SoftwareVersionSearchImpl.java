package cloud.xcan.sdf.core.angustester.application.query.version.impl;


import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.sdf.core.angustester.application.query.version.SoftwareVersionSearch;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class SoftwareVersionSearchImpl implements SoftwareVersionSearch {

  @Resource
  private SoftwareVersionSearchRepo versionSearchRepo;

  @Resource
  private SoftwareVersionQuery softwareVersionQuery;

  @Override
  public Page<SoftwareVersion> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<SoftwareVersion> clz, String... matches) {
    return new BizTemplate<Page<SoftwareVersion>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<SoftwareVersion> process() {
        Page<SoftwareVersion> page = versionSearchRepo.find(criteria, pageable, clz, matches);
        softwareVersionQuery.setVersionProgress(page.getContent());
        return page;
      }
    }.execute();
  }
}
