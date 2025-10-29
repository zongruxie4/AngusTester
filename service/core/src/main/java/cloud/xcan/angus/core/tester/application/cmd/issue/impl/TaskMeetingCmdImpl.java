package cloud.xcan.angus.core.tester.application.cmd.issue.impl;

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
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskMeetingCmd;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingRepo;
import cloud.xcan.angus.core.utils.CoreUtils;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task meeting command operations for meeting management.
 *
 * <p>This class provides functionality for managing task-related meetings,
 * including creation, updates, and deletion of meeting records.</p>
 *
 * <p>It handles the complete lifecycle of task meetings from creation
 * to deletion, including permission validation and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Meeting CRUD operations with permission validation</li>
 *   <li>Meeting replacement with upsert functionality</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Project member permission management</li>
 * </ul></p>
 */
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

  /**
   * Adds a new task meeting with permission validation.
   *
   * <p>This method creates a new meeting after verifying
   * the user has project member permissions.</p>
   *
   * <p>The method logs meeting creation activity for audit purposes.</p>
   *
   * @param meeting the meeting to add
   * @return the ID key of the created meeting
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskMeeting meeting) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Verify user has project member permissions
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

  /**
   * Updates an existing task meeting.
   *
   * <p>This method updates a meeting after verifying it exists
   * and logs the update activity for audit purposes.</p>
   *
   * @param meeting the meeting to update
   * @throws IllegalArgumentException if validation fails
   */
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

  /**
   * Replaces a task meeting with upsert functionality.
   *
   * <p>This method either updates an existing meeting or creates a new one
   * based on whether the meeting ID is provided.</p>
   *
   * <p>The method logs meeting update activity for audit purposes.</p>
   *
   * @param meeting the meeting to replace
   * @return the ID key of the meeting
   * @throws IllegalArgumentException if validation fails
   */
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

  /**
   * Deletes a task meeting with activity logging.
   *
   * <p>This method removes a meeting after verifying it exists
   * and logs the deletion activity for audit purposes.</p>
   *
   * @param id the meeting ID to delete
   * @throws IllegalArgumentException if validation fails
   */
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

  /**
   * Returns the repository instance for this command.
   *
   * @return the task meeting repository
   */
  @Override
  protected BaseRepository<TaskMeeting, Long> getRepository() {
    return taskMeetingRepo;
  }
}
