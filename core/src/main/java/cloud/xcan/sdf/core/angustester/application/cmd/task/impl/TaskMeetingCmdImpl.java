package cloud.xcan.sdf.core.angustester.application.cmd.task.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.MEETING;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskMeetingCmd;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectMemberQuery;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskMeetingQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
        projectMemberQuery.checkMember(meeting.getProjectId(), getUserId());
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
