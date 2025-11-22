package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.report;

import cloud.xcan.angus.core.tester.domain.report.ReportInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("reportInfoRepo")
public interface ReportInfoRepoMysql extends ReportInfoRepo {

}
