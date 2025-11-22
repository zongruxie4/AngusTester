package cloud.xcan.angus.core.tester.application.query.report;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuthCurrent;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ReportAuthQuery {

  Boolean status(Long reportId);

  List<ReportPermission> userAuth(Long reportId, Long userId, Boolean admin);

  ReportAuthCurrent currentUserAuth(Long reportId, Boolean admin);

  Map<Long, ReportAuthCurrent> currentUserAuths(HashSet<Long> reportIds, Boolean admin);

  void check(Long reportId, ReportPermission permission, Long userId);

  Page<ReportAuth> find(Specification<ReportAuth> spec, List<String> reportId,
      Pageable pageable);

  ReportAuth checkAndFind(Long id);

  void checkShareExportAuth(Long userId, Long reportId);

  void checkGrantAuth(Long userId, Long reportId);

  void checkViewReportAuth(Long userId, Long reportId);

  void checkModifyReportAuth(Long userId, Long reportId);

  void checkGenerateReportAuth(Long userId, Long reportId);

  void checkDeleteReportAuth(Long userId, Long reportId);

  void checkShareReportAuth(Long userId, Long reportId);

  void checkAuth(Long userId, Long reportId, ReportPermission permission);

  void checkAuth(Long userId, Long reportId, ReportPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> reportIds, ReportPermission permission);

  void checkRepeatAuth(Long reportId, Long authObjectId, AuthObjectType authObjectType);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, ReportPermission permission);

  List<ReportAuth> findAuth(Long userId, Long reportId);

  List<ReportAuth> findAuth(Long userId, Collection<Long> reportIds);

  List<ReportPermission> getUserAuth(Long reportId, Long userId);

  boolean isAdminUser();

  boolean isCreator(Long userId, Long reportId);

}




