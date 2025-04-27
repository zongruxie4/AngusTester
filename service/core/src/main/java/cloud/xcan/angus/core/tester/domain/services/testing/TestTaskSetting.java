package cloud.xcan.angus.core.tester.domain.services.testing;


import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.model.script.TestType;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class TestTaskSetting {

  private Long assigneeId;

  private TestType testType;

  private Priority priority;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TestTaskSetting)) {
      return false;
    }
    TestTaskSetting that = (TestTaskSetting) o;
    return Objects.equals(assigneeId, that.assigneeId) &&
        testType == that.testType && priority == that.priority &&
        Objects.equals(startDate, that.startDate) &&
        Objects.equals(deadlineDate, that.deadlineDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(assigneeId, testType, priority, startDate, deadlineDate);
  }
}
