package cloud.xcan.sdf.core.angustester.application.query.data.impl;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.application.query.data.DatasetSearch;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.DatasetSearchRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Biz
public class DatasetSearchImpl implements DatasetSearch {

  @Resource
  private DatasetSearchRepo datasetSearchRepo;

  @Override
  public Page<Dataset> search(Set<SearchCriteria> criterias, Pageable pageable, Class<Dataset> clz,
      String... matches) {
    return new BizTemplate<Page<Dataset>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<Dataset> process() {
        return datasetSearchRepo.find(criterias, pageable, clz, matches);
      }
    }.execute();
  }

}




