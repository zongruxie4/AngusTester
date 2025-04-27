package cloud.xcan.angus.core.tester.application.query.common;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.commonlink.setting.quota.Quota;
import cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource;
import cloud.xcan.angus.api.commonlink.setting.tenant.quota.SettingTenantQuota;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetInfo;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.CombinedTarget;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
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
