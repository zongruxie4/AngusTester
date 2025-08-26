package cloud.xcan.angus.core.tester.domain.services;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "services")
@SQLRestriction("deleted = 0")
@SQLDelete(sql = "update services set deleted = 1 where id = ?")
@DynamicInsert
@DynamicUpdate
@Setter
@Getter
@Accessors(chain = true)
public class Services extends TenantAuditingEntity<Services, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Enumerated(EnumType.STRING)
  private ApiSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "import_source")
  private ApiImportSource importSource;

  @Column(name = "name")
  private String name;

  @Column(name = "auth")
  private Boolean auth;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ApiStatus status;

  @Column(name = "deleted")
  private Boolean deleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deletedDate;

  @Transient
  private Long apisNum;
  @Transient
  private Long apisCaseNum;
  @Transient
  private Boolean hasApis;
  @Transient
  private Long mockServiceId;

  public boolean isEnabledAuth() {
    return nonNull(auth) && auth;
  }

  public boolean isReleased() {
    return nonNull(status) && status.isReleased();
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Services project)) {
      return false;
    }
    return Objects.equals(id, project.id) &&
        source == project.source &&
        importSource == project.importSource &&
        Objects.equals(name, project.name) &&
        Objects.equals(auth, project.auth) &&
        status == project.status &&
        Objects.equals(deleted, project.deleted) &&
        Objects.equals(deletedBy, project.deletedBy) &&
        Objects.equals(deletedDate, project.deletedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, source, importSource, name, auth, status, deleted,
        deletedBy, deletedDate);
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
