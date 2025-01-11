package cloud.xcan.sdf.core.angustester.interfaces.activity.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.query.activity.ActivityQuery;
import cloud.xcan.sdf.core.angustester.application.query.activity.ActivitySearch;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.ActivityFacade;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.internal.assembler.ActivityAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.vo.ActivityDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ActivityFacadeImpl implements ActivityFacade {

  @Resource
  private ActivityQuery activityQuery;

  @Resource
  private ActivitySearch activitySearch;

  @NameJoin
  @Override
  public PageResult<ActivityDetailVo> list(ActivityFindDto dto) {
    Page<Activity> activitiesPage = activityQuery
        .find(ActivityAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(activitiesPage, ActivityAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<ActivityDetailVo> search(ActivitySearchDto dto) {
    Page<Activity> activitiesPage = activitySearch
        .search(ActivityAssembler.getSearchCriteria(dto), dto.tranPage(), Activity.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(activitiesPage, ActivityAssembler::toDetailVo);
  }

}




