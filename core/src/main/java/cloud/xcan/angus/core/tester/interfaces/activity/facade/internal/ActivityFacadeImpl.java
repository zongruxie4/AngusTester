package cloud.xcan.angus.core.tester.interfaces.activity.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.query.activity.ActivityQuery;
import cloud.xcan.angus.core.tester.application.query.activity.ActivitySearch;
import cloud.xcan.angus.core.tester.domain.activity.Activity;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.ActivityFacade;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.dto.ActivitySearchDto;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.internal.assembler.ActivityAssembler;
import cloud.xcan.angus.core.tester.interfaces.activity.facade.vo.ActivityDetailVo;
import cloud.xcan.angus.remote.PageResult;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ActivityFacadeImpl implements ActivityFacade {

  @Resource
  private ActivityQuery activityQuery;

  @Resource
  private ActivitySearch activitySearch;

  @Override
  public PageResult<ActivityDetailVo> list(ActivityFindDto dto) {
    Page<Activity> activitiesPage = activityQuery
        .find(ActivityAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(activitiesPage, ActivityAssembler::toDetailVo);
  }

  @Override
  public PageResult<ActivityDetailVo> search(ActivitySearchDto dto) {
    Page<Activity> activitiesPage = activitySearch
        .search(ActivityAssembler.getSearchCriteria(dto), dto.tranPage(), Activity.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(activitiesPage, ActivityAssembler::toDetailVo);
  }

}




