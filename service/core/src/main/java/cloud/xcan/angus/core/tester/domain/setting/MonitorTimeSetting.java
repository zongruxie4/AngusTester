package cloud.xcan.angus.core.tester.domain.setting;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_TIME_FORMAT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.CreatedAt;
import cloud.xcan.angus.api.enums.DayOfWeek;
import cloud.xcan.angus.api.enums.PeriodicUnit;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
public class MonitorTimeSetting {

  @NotNull
  @Schema(description = "Creation at time way", requiredMode = RequiredMode.REQUIRED)
  private CreatedAt createdAt;

  private LocalDateTime createdAtSomeDate;

  private PeriodicUnit periodicCreationUnit;

  private DayOfWeek dayOfWeek;

  @Range(min = 1, max = 31)
  private Integer dayOfMonth;

  @DateTimeFormat(pattern = DEFAULT_TIME_FORMAT)
  private LocalTime timeOfDay;

  @Range(min = 1, max = 23)
  private Integer hourOfDay;

  @Range(min = 1, max = 59)
  private Integer minuteOfHour;

  private Boolean onetime;

  public boolean isOnetime() {
    return nonNull(onetime) ? onetime :
        CreatedAt.NOW == createdAt || createdAt == CreatedAt.AT_SOME_DATE;
  }

  public LocalDateTime getNextDate(LocalDateTime lastExecDate) {
    switch (createdAt) {
      case NOW:
        return LocalDateTime.now();
      case AT_SOME_DATE:
        assertNotNull(createdAtSomeDate, "Creation time not specified, createdAtSomeDate is null");
        return createdAtSomeDate;
      case PERIODICALLY:
        assertNotNull(periodicCreationUnit,
            "Periodic creation time unit not specified, periodicCreationUnit is null");
        LocalDateTime nowDateTime = LocalDateTime.now();
        LocalTime nowTime = nowDateTime.toLocalTime();
        switch (periodicCreationUnit) {
          case EVERY_MINUTE:
            assertNotNull(lastExecDate, "Last exec time not specified, lastExecDate is null");
            return lastExecDate.isBefore(nowDateTime) ? lastExecDate.plusMinutes(
                nullSafe(minuteOfHour, 1)) : lastExecDate;
          case HOURLY:
            assertNotNull(lastExecDate, "Last exec time not specified, lastExecDate is null");
            return lastExecDate.isBefore(nowDateTime) ? lastExecDate.plusHours(
                nullSafe(hourOfDay, 1)) : lastExecDate;
          case DAILY:
            assertNotNull(timeOfDay, "Daily creation time not specified, timeOfDay is null");
            return nowTime.isBefore(timeOfDay)
                ? LocalDateTime.of(nowDateTime.toLocalDate(), timeOfDay)
                : LocalDateTime.of(nowDateTime.plusDays(1).toLocalDate(), timeOfDay);
          case WEEKLY:
            assertNotNull(timeOfDay, "Daily creation time not specified, timeOfDay is null");
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
            assertNotNull(timeOfDay, "Daily creation time not specified, timeOfDay is null");
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
