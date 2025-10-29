package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollow;
import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowP;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
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
    if (isEmpty(dto.getTaskName()) || !dto.getFilters().isEmpty()) {
      if (dto.containsKey("taskName", SearchOperation.MATCH_END)
          || dto.containsKey("taskName", SearchOperation.MATCH)) {
        List<SearchCriteria> matchFilter = dto.findByKey("taskName");
        if (isNotEmpty(matchFilter)) {
          dto.setTaskName(matchFilter.get(0).getValue().toString());
        }
      }
    }
  }
}




