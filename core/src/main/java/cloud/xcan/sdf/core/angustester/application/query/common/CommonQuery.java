package cloud.xcan.sdf.core.angustester.application.query.common;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.quota.Quota;
import cloud.xcan.sdf.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.sdf.api.commonlink.setting.tenant.quota.SettingTenantQuota;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.sdf.api.commonlink.tag.OrgTargetType;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.CombinedTarget;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface CommonQuery {

  void checkAdminPermission();

  String checkAndGetAuthName(AuthObjectType authObjectType, Long authObjectId);

  Object checkAndGetIndicatorTarget(CombinedTargetType targetType, Long targetId);

  void checkIndicatorTargetModifyAuth(CombinedTargetType targetType, Long targetId);

  boolean isAdminUser();

  @NotNull
  Map<Long, UserInfo> getUserInfoMap(Collection<Long> userIds);

  CombinedTarget checkAndGetCombinedTarget(CombinedTargetType targetType, Long targetId,
      boolean findParent);

  CombinedTarget getCombinedTarget(CombinedTargetType targetType, Long targetId,
      boolean findParent);

  ActivityResource checkAndFindActivityResource(CombinedTargetType targetType, Long targetId);

  boolean checkAndSetAuthObjectIdCriteria(Set<SearchCriteria> criteria);

  void checkTenantQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr);

  void checkLcsQuota(QuotaResource quotaObject, Set<Long> objectIds, Long incr);

  SettingTenantQuota findTenantQuota(QuotaResource name);

  Map<String, List<NoticeType>> findTenantEventNoticeTypes(Long tenantId);

  void checkOrgExists(LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> typeIds);

  LinkedHashMap<OrgTargetType, LinkedHashSet<OrgTargetInfo>> findOrgs(
      LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> typeIds);

  void setInnerPrincipal(Long tenantId, Long userId);

  Quota findLcsQuota();

}
