package cloud.xcan.angus.core.tester.application.query.data.impl;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.data.DatasetSearch;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetSearchRepo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class DatasetSearchImpl implements DatasetSearch {

  @Resource
  private DatasetSearchRepo datasetSearchRepo;

  @Override
  public Page<Dataset> search(Set<SearchCriteria> criteria, Pageable pageable, Class<Dataset> clz,
      String... matches) {
    return new BizTemplate<Page<Dataset>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Dataset> process() {
        return datasetSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }

}




