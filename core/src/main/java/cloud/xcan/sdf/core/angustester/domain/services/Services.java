package cloud.xcan.sdf.core.angustester.domain.services;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.Objects;
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
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "services")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Where(clause = "deleted_flag = 0")
@SQLDelete(sql = "update services set deleted_flag = 1 where id = ?")
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

  @Column(name = "auth_flag")
  private Boolean authFlag;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ApiStatus status;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

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
  private Boolean hasApisFlag;
  @Transient
  private Long mockServiceId;

  public boolean isEnabledAuth() {
    return nonNull(authFlag) && authFlag;
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
    if (!(o instanceof Services)) {
      return false;
    }
    Services project = (Services) o;
    return Objects.equals(id, project.id) &&
        source == project.source &&
        importSource == project.importSource &&
        Objects.equals(name, project.name) &&
        Objects.equals(authFlag, project.authFlag) &&
        status == project.status &&
        Objects.equals(deletedFlag, project.deletedFlag) &&
        Objects.equals(deletedBy, project.deletedBy) &&
        Objects.equals(deletedDate, project.deletedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, source, importSource, name, authFlag, status, deletedFlag,
        deletedBy, deletedDate);
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
