package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.core.tester.application.converter.TaskMeetingConverter.getMeetingCreatorResourcesFilter;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.task.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Biz
public class TaskMeetingQueryImpl implements TaskMeetingQuery {

  @Resource
  private TaskMeetingRepo taskMeetingRepo;

  @Resource
  private UserManager userManager;

  @Override
  public TaskMeeting detail(Long id) {
    return new BizTemplate<TaskMeeting>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected TaskMeeting process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  @Override
  public Page<TaskMeeting> find(Specification<TaskMeeting> spec, Pageable pageable) {
    return new BizTemplate<Page<TaskMeeting>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<TaskMeeting> process() {
        return taskMeetingRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public List<TaskMeeting> findBySprintId(Long sprintId) {
    return taskMeetingRepo.findBySprintId(sprintId);
  }

  @Override
  public TaskMeeting checkAndFind(Long id) {
    return taskMeetingRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "TaskMeeting"));
  }

  @Override
  public List<TaskMeeting> getMeetingCreatedSummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId) {
    // Find all when condition is null, else find by condition
    Set<Long> creatorIds = Objects.isNull(creatorOrgType) ? null
        : userManager.getUserIdByOrgType0(creatorOrgType, creatorOrgId);
    Set<SearchCriteria> allFilters = getMeetingCreatorResourcesFilter(projectId, sprintId,
        createdDateStart, createdDateEnd, creatorIds);
    return taskMeetingRepo.findAllByFilters(allFilters);
  }
}
