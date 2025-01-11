package cloud.xcan.sdf.core.angustester.interfaces.activity.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.activity.Activity;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.dto.ActivitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.activity.facade.vo.ActivityDetailVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class ActivityAssembler {

  public static ActivityDetailVo toDetailVo(Activity activity) {
    return new ActivityDetailVo().setId(activity.getId())
        .setProjectId(activity.getProjectId())
        .setUserId(activity.getUserId())
        .setFullname(activity.getFullname()).setAvatar(activity.getAvatar())
        .setTargetId(activity.getTargetId()).setTargetType(activity.getTargetType())
        .setTargetName(activity.getTargetName())
        .setParentTargetId(activity.getParentTargetId())
        .setOptDate(activity.getOptDate())
        .setDescription(activity.getDescription())
        .setDetail(activity.getDetail());
  }

  public static GenericSpecification<Activity> getSpecification(ActivityFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "optDate")
        .orderByFields("id", "optDate")
        .matchSearchFields("targetName", "detail")
        .inAndNotFields("taskId", "targetId", "targetType")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(ActivitySearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "optDate")
        .orderByFields("id", "optDate")
        .matchSearchFields("targetName", "detail")
        .inAndNotFields("taskId", "targetId", "targetType")
        .build();
  }
}




