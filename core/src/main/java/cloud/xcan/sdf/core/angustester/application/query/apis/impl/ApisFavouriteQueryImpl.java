package cloud.xcan.sdf.core.angustester.application.query.apis.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

import cloud.xcan.sdf.core.angustester.application.query.apis.ApisFavouriteQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteP;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import javax.annotation.Resource;
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
      protected void checkParams() {
        // NOOP
      }

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
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Long process() {
        return isNull(projectId) ? apisFavouriteRepo.countByCreatedBy(getUserId())
            : apisFavouriteRepo.countByProjectIdAndCreatedBy(projectId, getUserId());
      }
    }.execute();
  }

}




