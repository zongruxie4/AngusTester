package cloud.xcan.angus.core.tester.domain.report.record;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ReportRecordInfoRepo extends BaseRepository<ReportRecordInfo, Long>,
    NameJoinRepository<ReportRecordInfo, Long> {

}
