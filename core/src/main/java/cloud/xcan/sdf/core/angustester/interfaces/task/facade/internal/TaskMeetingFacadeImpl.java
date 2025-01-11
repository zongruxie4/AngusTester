package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskMeetingCmd;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskMeetingQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskMeetingSearch;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskMeetingFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskMeetingAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class TaskMeetingFacadeImpl implements TaskMeetingFacade {

  @Resource
  private TaskMeetingQuery taskMeetingQuery;

  @Resource
  private TaskMeetingSearch taskMeetingSearch;

  @Resource
  private TaskMeetingCmd taskMeetingCmd;

  @Override
  public IdKey<Long, Object> add(TaskMeetingAddDto dto) {
    return taskMeetingCmd.add(TaskMeetingAssembler.addToDomain(dto));
  }

  @Override
  public void update(TaskMeetingUpdateDto dto) {
    taskMeetingCmd.update(TaskMeetingAssembler.updateToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(TaskMeetingReplaceDto dto) {
    return taskMeetingCmd.replace(TaskMeetingAssembler.replaceToDomain(dto));
  }

  @Override
  public void delete(Long id) {
    taskMeetingCmd.delete(id);
  }

  @Override
  public TaskMeetingDetailVo detail(Long id) {
    TaskMeeting meeting = taskMeetingQuery.detail(id);
    return TaskMeetingAssembler.toDetailVo(meeting);
  }

  @NameJoin
  @Override
  public PageResult<TaskMeetingVo> list(TaskMeetingFindDto dto) {
    Page<TaskMeeting> page = taskMeetingQuery
        .find(TaskMeetingAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, TaskMeetingAssembler::toVo);
  }

  @NameJoin
  @Override
  public PageResult<TaskMeetingVo> search(TaskMeetingSearchDto dto) {
    Page<TaskMeeting> page = taskMeetingSearch
        .search(TaskMeetingAssembler.getSearchCriteria(dto), dto.tranPage(),
            TaskMeeting.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, TaskMeetingAssembler::toVo);
  }
}
