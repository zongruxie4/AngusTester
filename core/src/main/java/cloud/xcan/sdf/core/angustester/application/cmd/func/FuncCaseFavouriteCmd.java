package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.favourite.FuncCaseFavourite;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface FuncCaseFavouriteCmd {

  IdKey<Long, Object> add(FuncCaseFavourite favourite);

  void cancel(Long caseId);

  void cancelAll(Long projectId);

}




