package cloud.xcan.sdf.core.angustester.domain.apis.auth;


import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "apis_auth")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class ApisAuth extends TenantEntity<ApisAuth, Long> {

  @Id
  private Long id;

  @Column(name = "apis_id")
  private Long apisId;

  @Column(name = "auth_object_type")
  @Enumerated(EnumType.STRING)
  private AuthObjectType authObjectType;

  @Column(name = "auth_object_id")
  private Long authObjectId;

  @Column(name = "auths")
  @Type(type = "json")
  private List<ApiPermission> auths;

  @Column(name = "creator_flag")
  private Boolean creatorFlag;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  @Column(name = "created_date")
  @CreatedDate
  private Date createdDate;

  @Override
  public Long identity() {
    return this.id;
  }

  public boolean isCreatorAuth() {
    return Objects.nonNull(creatorFlag) && creatorFlag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApisAuth apisAuth = (ApisAuth) o;
    return apisId.equals(apisAuth.apisId) &&
        authObjectType == apisAuth.authObjectType &&
        authObjectId.equals(apisAuth.authObjectId) &&
        Objects.equals(creatorFlag, apisAuth.creatorFlag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apisId, authObjectType, authObjectId, creatorFlag);
  }
}
