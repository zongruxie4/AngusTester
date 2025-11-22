package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ReportAuthConverter {

  @NotNull
  public static List<ReportAuth> toReportAuths(Set<Long> creatorIds, Long reportId
      , List<ReportPermission> auths, boolean creator) {
    return creatorIds.stream()
        .map(creatorId -> new ReportAuth()
            .setId(Objects.requireNonNull(getBean(CachedUidGenerator.class)).getUID())
            .setReportId(reportId)
            .setAuthObjectType(AuthObjectType.USER)
            .setAuthObjectId(creatorId)
            .setAuths(auths)
            .setCreator(creator))
        .collect(Collectors.toList());
  }

}
