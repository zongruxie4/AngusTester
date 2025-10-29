package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavourite;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface FuncCaseFavouriteCmd {

  IdKey<Long, Object> add(FuncCaseFavourite favourite);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

}




