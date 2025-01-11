package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "analysis")
@Setter
@Getter
@Accessors(chain = true)
public class Analysis extends TenantAuditingEntity<Analysis, Long> implements ActivityResource,
    IdAndCreatedDateBase<Analysis> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Enumerated(EnumType.STRING)
  private AnalysisResource resource;

  /**
   * @see AnalysisCaseTemplate
   * @see AnalysisTaskTemplate
   */
  private String template;

  private String name;

  private String description;

  /**
   * @see AnalysisCaseObject
   * @see AnalysisTaskObject
   */
  private String object;
  @Column(name = "plan_id")
  private Long planId; // Sprint or plan id
  @Column(name = "org_type")
  private AuthObjectType orgType;
  @Column(name = "org_id")
  private Long orgId;

  @Column(name = "contains_user_analysis")
  private Boolean containsUserAnalysis;
  @Column(name = "contains_data_detail")
  private Boolean containsDataDetail;

  @Enumerated(EnumType.STRING)
  @Column(name = "time_range")
  private AnalysisTimeRange timeRange;
  @Column(name = "start_time")
  private LocalDateTime startTime;
  @Column(name = "end_time")
  private LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  private AnalysisDataSource datasource;

  @Column(name = "filter_criteria")
  private String filterCriteria;

  @Transient
  private AbstractOverview dataObj;

  public boolean isAnalysisPlan() {
    return AnalysisCaseObject.PLAN.getValue().equalsIgnoreCase(object)
        || AnalysisTaskObject.SPRINT.getValue().equalsIgnoreCase(object);
  }

  public boolean isAnalysisOrg() {
    return AnalysisCaseObject.TESTER_ORG.getValue().equalsIgnoreCase(object)
        || AnalysisTaskObject.ASSIGNEE_ORG.getValue().equalsIgnoreCase(object);
  }

  @Nullable
  public LocalDateTime getStartTime() {
    LocalDateTime now = LocalDateTime.now();
    switch (timeRange) {
      case ALL_TIME:
        return null;
      case THIS_WEEK:
        return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN);
      case LAST_WEEK:
        return now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).with(LocalTime.MIN)
            .minusWeeks(1);
      case THIS_MONTH:
        return now.toLocalDate().withDayOfMonth(1).atStartOfDay();
      case LAST_MONTH:
        LocalDate firstDayOfLastMonth = now.toLocalDate().withDayOfMonth(1).minusMonths(1);
        return firstDayOfLastMonth.atStartOfDay();
      case THIS_YEAR:
        LocalDate firstDayOfYear = now.toLocalDate().withDayOfYear(1);
        return firstDayOfYear.atStartOfDay();
      case CUSTOM_TIME:
        return endTime;
    }
    return endTime;
  }

  @Nullable
  public LocalDateTime getEndTime() {
    LocalDateTime now = LocalDateTime.now();
    switch (timeRange) {
      case ALL_TIME:
        return null;
      case THIS_WEEK:
        return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX);
      case LAST_WEEK:
        return now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).with(LocalTime.MAX)
            .minusWeeks(1);
      case THIS_MONTH:
        LocalDate firstDayOfNextMonth = now.toLocalDate().withDayOfMonth(1).plusMonths(1);
        return firstDayOfNextMonth.minusDays(1).atTime(LocalTime.MAX);
      case LAST_MONTH:
        LocalDate firstDayOfLastMonth = now.toLocalDate().withDayOfMonth(1).minusMonths(1);
        LocalDate firstDayOfLastNextMonth = firstDayOfLastMonth.plusMonths(1);
        return firstDayOfLastNextMonth.minusDays(1).atTime(LocalTime.MAX);
      case THIS_YEAR:
        LocalDate firstDayOfYear = now.toLocalDate().withDayOfYear(1);
        LocalDate firstDayOfNextYear = firstDayOfYear.plusYears(1);
        return firstDayOfNextYear.minusDays(1).atTime(LocalTime.MAX);
      case CUSTOM_TIME:
        return endTime;
    }
    return endTime;
  }

  public boolean notSameConfigAs(Analysis analysis) {
    if (Objects.equals(analysis, this)) {
      return false;
    }
    return !this.configEquals(analysis);
  }

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return id;
  }

  public boolean configEquals(Analysis analysis) {
    if (this == analysis) {
      return true;
    }
    if (analysis == null) {
      return false;
    }
    return Objects.equals(object, analysis.object)
        && Objects.equals(planId, analysis.planId)
        && orgType == analysis.orgType
        && Objects.equals(orgId, analysis.orgId)
        && Objects.equals(containsUserAnalysis, analysis.containsUserAnalysis)
        && Objects.equals(containsDataDetail, analysis.containsDataDetail)
        && timeRange == analysis.timeRange
        && Objects.equals(startTime, analysis.startTime)
        && Objects.equals(endTime, analysis.endTime)
        && datasource == analysis.datasource
        && Objects.equals(filterCriteria, analysis.filterCriteria);
  }

}
