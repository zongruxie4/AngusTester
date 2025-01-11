package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.spring.SpringContextHolder.getBean;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuth;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ReportAuthConverter {

  @NotNull
  public static List<ReportAuth> toReportAuths(Set<Long> creatorIds, Long reportId
      , List<ReportPermission> auths, boolean creatorFlag) {
    return creatorIds.stream()
        .map(creatorId -> new ReportAuth()
            .setId(getBean(CachedUidGenerator.class).getUID())
            .setReportId(reportId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(auths)
            .setCreatorFlag(creatorFlag))
        .collect(Collectors.toList());
  }

}
