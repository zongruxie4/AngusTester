package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.report;

import cloud.xcan.angus.core.tester.domain.report.ReportInfoRepo;
import org.springframework.stereotype.Repository;

@Repository("reportInfoRepo")
public interface ReportInfoRepoPostgres extends ReportInfoRepo {

}
