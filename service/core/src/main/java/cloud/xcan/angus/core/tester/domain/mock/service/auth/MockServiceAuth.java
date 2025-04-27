package cloud.xcan.angus.core.tester.domain.mock.service.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.ResourceId;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author XiaoLong Liu
 */@Entity
@Table(name = "mock_service_auth")
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
  @Type(JsonType.class)
  private List<MockServicePermission> auths;

  @Column(name = "creator")
  private Boolean creator;

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
    return Objects.nonNull(creator) && creator;
  }
}
