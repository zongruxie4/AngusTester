package cloud.xcan.sdf.core.angustester.application.query.script;

import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuthCurrent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ScriptAuthQuery {

  Boolean status(Long scriptId);

  List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean adminFlag);

  ScriptAuthCurrent currentUserAuth(Long scriptId, Boolean adminFlag);

  Map<Long, ScriptAuthCurrent> currentUserAuths(HashSet<Long> scriptIds, Boolean adminFlag);

  void check(Long scriptId, ScriptPermission authPermission, Long userId);

  Page<ScriptAuth> find(Specification<ScriptAuth> spec,
      List<String> scriptIds, Pageable pageable);

  ScriptAuth checkAndFind(Long id);

  void checkViewAuth(Long userId, Long scriptId);

  void checkModifyAuth(Long userId, Long scriptId);

  void checkDeleteAuth(Long userId, Long scriptId);

  void checkTestAuth(Long userId, Long scriptId);

  void checkGrantAuth(Long userId, Long scriptId);

  void checkExportAuth(Long userId, Long scriptId);

  void checkAuth(Long userId, Long scriptId, ScriptPermission permission);

  void checkAuth(Long userId, Long scriptId, ScriptPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> scriptIds, ScriptPermission permission);

  void checkRepeatAuth(Long scriptId, Long authObjectId, AuthObjectType authObjectType);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, ScriptPermission permission);

  List<ScriptAuth> findAuth(Long userId, Long scriptId);

  List<ScriptAuth> findAuth(Long userId, Collection<Long> scriptIds);

  List<ScriptPermission> getUserAuth(Long scriptId, Long userId);

  Map<Long, Set<ScriptPermission>> getUserScriptAuth(Collection<Long> scriptIds, Long userId);

  boolean isCreator(Long userId, Long scriptId);

}




