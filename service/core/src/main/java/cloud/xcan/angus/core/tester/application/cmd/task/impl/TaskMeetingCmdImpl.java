package cloud.xcan.angus.core.tester.application.cmd.task.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.MEETING;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskMeetingCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingRepo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class TaskMeetingCmdImpl extends CommCmd<TaskMeeting, Long> implements TaskMeetingCmd {

  @Resource
  private TaskMeetingRepo taskMeetingRepo;

  @Resource
  private TaskMeetingQuery taskMeetingQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskMeeting meeting) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), meeting.getProjectId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(meeting);

        activityCmd.add(toActivity(MEETING, meeting, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(TaskMeeting meeting) {
    new BizTemplate<Void>() {
      TaskMeeting meetingDb;

      @Override
      protected void checkParams() {
        meetingDb = taskMeetingQuery.checkAndFind(meeting.getId());
      }

      @Override
      protected Void process() {
        taskMeetingRepo.save(CoreUtils.copyPropertiesIgnoreNull(meeting, meetingDb));

        activityCmd.add(toActivity(MEETING, meetingDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(TaskMeeting meeting) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskMeeting meetingDb;

      @Override
      protected void checkParams() {
        if (nonNull(meeting.getId())) {
          meetingDb = taskMeetingQuery.checkAndFind(meeting.getId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(meetingDb)) {
          return add(meeting);
        }

        taskMeetingRepo.save(CoreUtils.copyProperties(meeting, meetingDb));

        activityCmd.add(toActivity(MEETING, meetingDb, ActivityType.UPDATED));
        return new IdKey<>(meetingDb.getId(), meetingDb.getSubject());
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      TaskMeeting meetingDb;

      @Override
      protected void checkParams() {
        meetingDb = taskMeetingQuery.checkAndFind(id);
      }

      @Override
      protected Void process() {
        taskMeetingRepo.deleteById(id);

        activityCmd.add(toActivity(MEETING, meetingDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<TaskMeeting, Long> getRepository() {
    return taskMeetingRepo;
  }
}
