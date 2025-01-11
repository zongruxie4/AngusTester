package cloud.xcan.sdf.core.angustester.domain.mock.service.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.biz.ResourceId;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantEntity;
import java.time.LocalDateTime;
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

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "mock_service_auth")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class MockServiceAuth extends TenantEntity<MockServiceAuth, Long> {

  @Id
  private Long id;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @Column(name = "auth_object_type")
  @Enumerated(EnumType.STRING)
  private AuthObjectType authObjectType;

  @ResourceId
  @Column(name = "auth_object_id")
  private Long authObjectId;

  @Column(name = "auths", columnDefinition = "json")
  @Type(type = "json")
  private List<MockServicePermission> auths;

  @Column(name = "creator_flag")
  private Boolean creatorFlag;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  @Column(name = "created_date")
  @CreatedDate
  private LocalDateTime createdDate;

  @Override
  public Long identity() {
    return this.id;
  }

  public boolean isCreatorAuth() {
    return Objects.nonNull(creatorFlag) && creatorFlag;
  }
}
