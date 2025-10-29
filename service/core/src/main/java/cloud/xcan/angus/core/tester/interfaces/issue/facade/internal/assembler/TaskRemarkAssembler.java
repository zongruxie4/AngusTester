package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.issue.remark.TaskRemark;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

/**
 * @author XiaoLong Liu
 */
public class TaskRemarkAssembler {

  public static TaskRemark toTaskRemark(TaskRemarkAddDto dto) {
    return new TaskRemark().setTaskId(dto.getTaskId())
        .setContent(dto.getContent());
  }

  public static TaskRemarkVo toTaskRemarkVo(TaskRemark remark) {
    return new TaskRemarkVo().setId(remark.getId())
        .setTaskId(remark.getTaskId())
        .setCreatedBy(remark.getCreatedBy())
        .setCreatedDate(remark.getCreatedDate())
        .setContent(remark.getContent());
  }

  public static GenericSpecification<TaskRemark> getSpecification(TaskRemarkFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .orderByFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }
}
