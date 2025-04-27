package cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollow;
import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.List;


public class TaskFollowAssembler {

  public static TaskFollow addDtoToDomain(Long taskId) {
    return new TaskFollow().setTaskId(taskId);
  }

  public static TaskFollowDetailVo toDetailVo(TaskFollowP follow) {
    return new TaskFollowDetailVo()
        .setId(follow.getId())
        .setProjectId(follow.getProjectId())
        .setSprintId(follow.getSprintId())
        .setTaskId(follow.getTaskId())
        .setTaskName(follow.getTaskName())
        .setTaskCode(follow.getTaskCode())
        .setTaskType(TaskType.valueOf(follow.getTaskType()));
  }

  public static void judgeMatchFilter(TaskFollowFindDto dto) {
    // Build the final filters
    new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("taskName")
        .build();
    if (ObjectUtils.isEmpty(dto.getTaskName()) || !dto.getFilters().isEmpty()) {
      if (dto.containsKey("taskName", SearchOperation.MATCH_END) || dto
          .containsKey("taskName", SearchOperation.MATCH)) {
        List<SearchCriteria> matchFilter = dto.findByKey("taskName");
        if (ObjectUtils.isNotEmpty(matchFilter)) {
          dto.setTaskName(matchFilter.get(0).getValue().toString());
        }
      }
    }
  }
}




