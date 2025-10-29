package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.core.tester.application.converter.TaskMeetingConverter.getMeetingCreatorResourcesFilter;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.issue.TaskMeetingQuery;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingRepo;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingSearchRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * <p>
 * Implementation of TaskMeetingQuery for task meeting management and query operations.
 * </p>
 * <p>
 * Provides methods for task meeting CRUD operations, search functionality, and meeting summary statistics.
 * </p>
 */
@Biz
public class TaskMeetingQueryImpl implements TaskMeetingQuery {

  @Resource
  private TaskMeetingRepo taskMeetingRepo;
  @Resource
  private TaskMeetingSearchRepo taskMeetingSearchRepo;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Get detailed information of a task meeting by ID.
   * </p>
   * <p>
   * Retrieves and validates the existence of a task meeting, throwing ResourceNotFound if not found.
   * </p>
   * @param id Task meeting ID
   * @return Task meeting details
   */
  @Override
  public TaskMeeting detail(Long id) {
    return new BizTemplate<TaskMeeting>() {

      @Override
      protected TaskMeeting process() {
        return checkAndFind(id);
      }
    }.execute();
  }

  /**
   * <p>
   * List task meetings with pagination and search capabilities.
   * </p>
   * <p>
   * Supports both full-text search and specification-based filtering.
   * Uses different repositories based on search type for optimal performance.
   * </p>
   * @param spec Generic specification for filtering
   * @param pageable Pagination information
   * @param fullTextSearch Whether to use full-text search
   * @param match Fields to match in full-text search
   * @return Page of task meetings
   */
  @Override
  public Page<TaskMeeting> list(GenericSpecification<TaskMeeting> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match) {
    return new BizTemplate<Page<TaskMeeting>>() {
      @Override
      protected Page<TaskMeeting> process() {
        return fullTextSearch
            ? taskMeetingSearchRepo.find(spec.getCriteria(), pageable, TaskMeeting.class, match)
            : taskMeetingRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Find task meetings by sprint ID.
   * </p>
   * <p>
   * Retrieves all task meetings associated with a specific sprint.
   * </p>
   * @param sprintId Sprint ID
   * @return List of task meetings in the sprint
   */
  @Override
  public List<TaskMeeting> findBySprintId(Long sprintId) {
    return taskMeetingRepo.findBySprintId(sprintId);
  }

  /**
   * <p>
   * Check and find a task meeting by ID.
   * </p>
   * <p>
   * Validates the existence of a task meeting and throws ResourceNotFound if not found.
   * Used for validation before operations that require an existing meeting.
   * </p>
   * @param id Task meeting ID
   * @return Task meeting if found
   * @throws ResourceNotFound if task meeting not found
   */
  @Override
  public TaskMeeting checkAndFind(Long id) {
    return taskMeetingRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "TaskMeeting"));
  }

  /**
   * <p>
   * Get meeting creation summaries with various filtering criteria.
   * </p>
   * <p>
   * Retrieves meeting summaries filtered by project, sprint, date range, and creator organization.
   * Supports flexible filtering where null conditions return all results.
   * </p>
   * @param projectId Project ID filter
   * @param sprintId Sprint ID filter
   * @param createdDateStart Start date for creation time filter
   * @param createdDateEnd End date for creation time filter
   * @param creatorOrgType Creator organization type filter
   * @param creatorOrgId Creator organization ID filter
   * @return List of meeting summaries matching the criteria
   */
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
