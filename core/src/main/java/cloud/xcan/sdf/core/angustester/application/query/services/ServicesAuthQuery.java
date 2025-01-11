package cloud.xcan.sdf.core.angustester.application.query.services;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuth;
import cloud.xcan.sdf.core.angustester.domain.services.auth.ServicesAuthCurrent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ServicesAuthQuery {

  Boolean status(Long serviceId);

  List<ServicesPermission> userAuth(Long serviceId, Long userId, Boolean adminFlag);

  ServicesAuthCurrent currentUserAuth(Long serviceId, Boolean adminFlag);

  void check(Long serviceId, ServicesPermission authPermission, Long authObjectId);

  Page<ServicesAuth> find(Specification<ServicesAuth> spec, List<String> serviceIds,
      Pageable pageable);

  ServicesAuth checkAndFind(Long id);

  void checkAddAuth(Long userId, Long serviceId);

  void checkViewAuth(Long userId, Long serviceId);

  void checkModifyAuth(Long userId, Long serviceId);

  void checkDeleteAuth(Long userId, Long serviceId);

  void checkTestAuth(Long userId, Long serviceId);

  void checkGrantAuth(Long userId, Long serviceId);

  void checkShareAuth(Long userId, Long serviceId);

  void checkReleaseAuth(Long userId, Long serviceId);

  void checkAuth(Long userId, Long serviceId, ServicesPermission permission);

  void checkAuth(Long userId, Long serviceId, ServicesPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> serviceIds, ServicesPermission permission);

  void checkRepeatAuth(Long serviceId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creatorFlag);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, ServicesPermission permission);

  List<ServicesAuth> findAuth(Long userId, Long serviceId);

  List<ServicesAuth> findAuth(Long userId, Collection<Long> serviceIds);

  List<ServicesPermission> getUserAuth(Long serviceId, Long userId);

  boolean isCreator(Long userId, Long serviceId);

}




