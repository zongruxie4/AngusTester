package cloud.xcan.angus.core.tester.interfaces.issue.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler.addToDomain;
import static cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler.replaceToDomain;
import static cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler.updateToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskMeetingCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.TaskMeetingFacade;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.internal.assembler.TaskMeetingAssembler;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskMeetingFacadeImpl implements TaskMeetingFacade {

  @Resource
  private TaskMeetingQuery taskMeetingQuery;

  @Resource
  private TaskMeetingCmd taskMeetingCmd;

  @Override
  public IdKey<Long, Object> add(TaskMeetingAddDto dto) {
    return taskMeetingCmd.add(addToDomain(dto));
  }

  @Override
  public void update(TaskMeetingUpdateDto dto) {
    taskMeetingCmd.update(updateToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(TaskMeetingReplaceDto dto) {
    return taskMeetingCmd.replace(replaceToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    taskMeetingCmd.delete(id);
  }

  @Override
  public TaskMeetingDetailVo detail(Long id) {
    return toDetailVo(taskMeetingQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<TaskMeetingVo> list(TaskMeetingFindDto dto) {
    Page<TaskMeeting> page = taskMeetingQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskMeetingAssembler::toVo);
  }

}
