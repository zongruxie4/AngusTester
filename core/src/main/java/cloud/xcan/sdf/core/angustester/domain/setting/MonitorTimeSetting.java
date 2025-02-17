package cloud.xcan.sdf.core.angustester.domain.setting;

import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotNull;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_TIME_FORMAT;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.api.enums.DayOfWeek;
import cloud.xcan.sdf.api.enums.PeriodicUnit;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.validation.constraints.NotNull;
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
  @ApiModelProperty(value = "Creation at time way", required = true)
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

  public boolean isOnetime() {
    return CreatedAt.NOW == createdAt || createdAt == CreatedAt.AT_SOME_DATE;
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
        assertNotNull(timeOfDay, "Daily creation time not specified, timeOfDay is null");
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
