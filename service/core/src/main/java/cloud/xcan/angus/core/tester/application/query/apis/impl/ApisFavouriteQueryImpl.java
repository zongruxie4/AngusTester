package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.query.apis.ApisFavouriteQuery;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ApisFavouriteQueryImpl implements ApisFavouriteQuery {

  @Resource
  private ApisFavouriteRepo apisFavouriteRepo;

  @Override
  public Page<ApisFavouriteP> search(Long projectId, String apisName, PageRequest pageable) {
    return new BizTemplate<Page<ApisFavouriteP>>() {

      @Override
      protected Page<ApisFavouriteP> process() {
        return isEmpty(apisName) ? apisFavouriteRepo.search(projectId, getUserId(), pageable)
            : apisFavouriteRepo.searchByMatch(projectId, getUserId(), apisName, pageable);
      }
    }.execute();
  }

  @Override
  public Long count(Long projectId) {
    return new BizTemplate<Long>() {

      @Override
      protected Long process() {
        return isNull(projectId) ? apisFavouriteRepo.countByCreatedBy(getUserId())
            : apisFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}




