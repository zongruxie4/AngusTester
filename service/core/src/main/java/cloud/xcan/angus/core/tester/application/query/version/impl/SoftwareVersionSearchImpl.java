package cloud.xcan.angus.core.tester.application.query.version.impl;


import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionQuery;
import cloud.xcan.angus.core.tester.application.query.version.SoftwareVersionSearch;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
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
      protected Page<SoftwareVersion> process() {
        Page<SoftwareVersion> page = versionSearchRepo.find(criteria, pageable, clz, matches);
        softwareVersionQuery.setVersionProgress(page.getContent());
        return page;
      }
    }.execute();
  }
}
