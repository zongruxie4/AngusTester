package cloud.xcan.angus.core.tester.domain.apis.auth;


import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "apis_auth")
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
  @Type(JsonType.class)
  private List<ApiPermission> auths;

  @Column(name = "creator")
  private Boolean creator;

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
    return Objects.nonNull(creator) && creator;
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
        Objects.equals(creator, apisAuth.creator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apisId, authObjectType, authObjectId, creator);
  }
}
