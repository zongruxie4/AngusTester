package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.config;

import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFuncRepo;
import org.springframework.stereotype.Repository;

@Repository("indicatorFuncRepo")
public interface IndicatorFuncRepoMysql extends IndicatorFuncRepo {


}
