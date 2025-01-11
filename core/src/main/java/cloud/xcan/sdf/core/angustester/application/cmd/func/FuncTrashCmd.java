package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import java.util.List;

public interface FuncTrashCmd {

  void add0(List<FuncTrash> trashes);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

}
