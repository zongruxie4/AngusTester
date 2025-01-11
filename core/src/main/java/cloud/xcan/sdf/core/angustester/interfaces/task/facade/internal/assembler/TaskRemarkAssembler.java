package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.remark.TaskRemark;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

/**
 * @author xiaolong.liu
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
