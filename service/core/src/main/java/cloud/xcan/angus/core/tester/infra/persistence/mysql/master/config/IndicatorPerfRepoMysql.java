package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorPerfRepo;
import org.springframework.stereotype.Repository;

@Repository("indicatorPerfRepo")
public interface IndicatorPerfRepoMysql extends IndicatorPerfRepo {


}
