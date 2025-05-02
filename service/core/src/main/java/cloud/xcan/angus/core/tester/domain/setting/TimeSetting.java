package cloud.xcan.angus.core.tester.domain.setting;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_TIME_FORMAT;

import cloud.xcan.angus.api.enums.CreatedAt;
import cloud.xcan.angus.api.enums.DayOfWeek;
import cloud.xcan.angus.api.enums.PeriodicCreationUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
@Accessors(chain = true)
public class TimeSetting {

  @NotNull
  @Schema(description = "Creation at time way", required = true)
  private CreatedAt createdAt;

  private LocalDateTime createdAtSomeDate;

  private PeriodicCreationUnit periodicCreationUnit;

  private DayOfWeek dayOfWeek;

  @Range(min = 1, max = 31)
  private Integer dayOfMonth;

  @DateTimeFormat(pattern = DEFAULT_TIME_FORMAT)
  private LocalTime timeOfDay;

  @JsonIgnore
  public boolean isOnetime() {
    return CreatedAt.NOW == createdAt || createdAt == CreatedAt.AT_SOME_DATE;
  }

  @JsonIgnore
  public LocalDateTime getNextDate() {
    switch (createdAt) {
      case NOW:
        return LocalDateTime.now();
      case AT_SOME_DATE:
        assertNotNull(createdAtSomeDate, "Creation time not specified, createdAtSomeDate is null");
        return createdAtSomeDate;
      case PERIODICALLY:
        assertNotNull(periodicCreationUnit,
            "Periodic creation time unit not specified, periodicCreationUnit is null");
        assertNotNull(timeOfDay, "Daily creation time not specified, timeOfDay is null");
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalTime nowTime = nowDateTime.toLocalTime();
        switch (periodicCreationUnit) {
          case DAILY:
            return nowTime.isBefore(timeOfDay)
                ? LocalDateTime.of(nowDateTime.toLocalDate(), timeOfDay)
                : LocalDateTime.of(nowDateTime.plusDays(1).toLocalDate(), timeOfDay);
          case WEEKLY:
            assertNotNull(dayOfWeek,
                "The day of creation in each week is not specified, dayOfWeek is null");
            java.time.DayOfWeek nowDayOfWeek = nowDateTime.getDayOfWeek();
            java.time.DayOfWeek dayOfWeek0 = java.time.DayOfWeek.of(dayOfWeek.getIntValue());
            return nowDayOfWeek.getValue() < dayOfWeek0.getValue()
                || (nowDayOfWeek.getValue() == dayOfWeek0.getValue() && nowTime.isBefore(timeOfDay))
                // Current week
                ? LocalDateTime.of(nowDateTime.plusDays(dayOfWeek0.getValue()
                - nowDayOfWeek.getValue()).toLocalDate(), timeOfDay)
                // Next week
                : LocalDateTime.of(nowDateTime.plusDays(7 - nowDayOfWeek.getValue()
                    + dayOfWeek0.getValue()).toLocalDate(), timeOfDay);
          case MONTHLY:
            assertNotNull(dayOfMonth,
                "The day of creation in each month is not specified, dayOfMonth is null");
            int nowDayOfMonth = nowDateTime.getDayOfMonth();
            return nowDayOfMonth < dayOfMonth
                || (nowDayOfMonth == dayOfMonth && nowTime.isBefore(timeOfDay))
                ? LocalDateTime.of(
                // Current month
                LocalDate.of(nowDateTime.getYear(), nowDateTime.getMonth(), dayOfMonth), timeOfDay)
                : LocalDateTime.of(
                    // Next month
                    LocalDate.of(nowDateTime.getYear(), nowDateTime.getMonth().plus(1), dayOfMonth),
                    timeOfDay);
        }
      default:
        return null;
    }
  }

}
