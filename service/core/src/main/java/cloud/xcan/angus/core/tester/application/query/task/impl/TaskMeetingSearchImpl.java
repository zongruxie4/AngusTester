package cloud.xcan.angus.core.tester.application.query.task.impl;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.application.query.task.TaskMeetingSearch;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingSearchRepo;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import java.util.Set;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class TaskMeetingSearchImpl implements TaskMeetingSearch {

  @Resource
  private TaskMeetingSearchRepo taskMeetingSearchRepo;

  @Override
  public Page<TaskMeeting> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<TaskMeeting> clz, String... matches) {
    return new BizTemplate<Page<TaskMeeting>>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Page<TaskMeeting> process() {
        return taskMeetingSearchRepo.find(criteria, pageable, clz, matches);
      }
    }.execute();
  }
}
