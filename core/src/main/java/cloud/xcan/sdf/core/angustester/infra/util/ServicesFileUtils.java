package cloud.xcan.sdf.core.angustester.infra.util;

import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getTenantId;
import static cloud.xcan.sdf.core.utils.CoreUtils.randomUUID;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.sdf.api.commonlink.TesterConstant;
import cloud.xcan.sdf.core.utils.SpringAppDirUtils;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.spec.utils.FileSearchUtils;
import cloud.xcan.sdf.spec.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;

public class ServicesFileUtils {

  @SneakyThrows
  public static List<File> getImportApiFiles(File tmpPath, File importFile) {
    List<File> importFiles = new ArrayList<>();
    if (FileUtils.isZipFile(importFile.toPath())) {
      try {
        FileUtils.extract(importFile.getPath(), tmpPath.getPath());
        importFiles = FileSearchUtils.newBuilder()
            .filter(pathname -> pathname.getName().endsWith(".json")
                || pathname.getName().endsWith(".yaml") || pathname.getName().endsWith(".txt"))
            .build().search();
        assertNotEmpty(importFiles,
            "Api specification file not found, it must be in JSON, YAML, or txt format");
      } catch (Exception e) {
        assertTrue(false,
            "The decompression file format is incorrect, it must be a valid zip file");
      }
    } else {
      importFiles.add(importFile);
    }
    return importFiles;
  }

  public static File getImportTmpPath(ApiImportSource sourceType, String fileName) {
    String tmpPath;
    SpringAppDirUtils utils = new SpringAppDirUtils();
    if (sourceType.equals(ApiImportSource.OPENAPI)) {
      tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_OPENAPI_DIR + getTenantId()
          + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    } else /*if (sourceType.equals(ApiImportSource.POSTMAN))*/ {
      tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_POSTMAN_DIR + getTenantId()
          + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    }
    File file = new File(tmpPath);
    file.mkdirs();
    return file;
  }

  public static File getExportTmpPath(String fileName) {
    SpringAppDirUtils utils = new SpringAppDirUtils();
    String tmpPath =
        utils.getTmpDir() + TesterConstant.EXPORT_OPENAPI_DIR + getTenantId() + File.separator
            + randomUUID() + File.separator + nullSafe(fileName, "");
    File file = new File(tmpPath);
    if (file.isDirectory()) {
      file.mkdirs();
    } else {
      file.getParentFile().mkdirs();
    }
    return file;
  }


}
