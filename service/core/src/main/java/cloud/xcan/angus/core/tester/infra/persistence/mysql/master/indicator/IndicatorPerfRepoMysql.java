package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.indicator;

import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerfRepo;
import org.springframework.stereotype.Repository;

@Repository("indicatorPerfRepo")
public interface IndicatorPerfRepoMysql extends IndicatorPerfRepo {


}
