package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.utils.CoreUtils.randomUUID;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.spec.utils.FileSearchUtils;
import cloud.xcan.angus.spec.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Utility class for API service file operations and import/export management.
 * <p>
 * Provides file handling utilities for OpenAPI and Postman specification imports.
 * <p>
 * Implements ZIP file extraction, file format validation, and temporary path management.
 * <p>
 * Supports multiple API import sources and ensures proper file organization by tenant.
 */
public class ServicesFileUtils {

  /**
   * Processes import files and extracts API specification files from various formats.
   * <p>
   * Handles both single files and ZIP archives containing API specifications.
   * <p>
   * Validates file formats and ensures proper extraction of specification files.
   * <p>
   * @param tmpPath the temporary directory for file processing
   * @param importFile the file to process (single file or ZIP archive)
   * @return list of extracted API specification files
   * @throws RuntimeException if file processing fails or invalid format detected
   */
  @SneakyThrows
  public static List<File> getImportApiFiles(File tmpPath, File importFile) {
    List<File> importFiles = new ArrayList<>();
    
    // Check if the import file is a ZIP archive
    if (FileUtils.isZipFile(importFile.toPath())) {
      try {
        // Extract ZIP file contents to temporary directory
        FileUtils.extract(importFile.getPath(), tmpPath.getPath());
        
        // Search for API specification files in extracted contents
        importFiles = FileSearchUtils.newBuilder()
            .filter(pathname -> pathname.getName().endsWith(".json")
                || pathname.getName().endsWith(".yaml") || pathname.getName().endsWith(".txt"))
            .build().search();
        
        // Validate that specification files were found
        assertNotEmpty(importFiles,
            "Api specification file not found, it must be in JSON, YAML, or txt format");
      } catch (Exception e) {
        // Handle ZIP extraction errors
        assertTrue(false,
            "The decompression file format is incorrect, it must be a valid zip file");
      }
    } else {
      // Add single file directly to import list
      importFiles.add(importFile);
    }
    return importFiles;
  }

  /**
   * Creates a temporary directory path for API import operations.
   * <p>
   * Generates tenant-specific temporary directories for different import sources.
   * <p>
   * Supports OpenAPI and Postman import sources with separate directory structures.
   * <p>
   * @param sourceType the type of API import source (OPENAPI or POSTMAN)
   * @param fileName optional file name to include in the path
   * @return the created temporary directory for import operations
   */
  public static File getImportTmpPath(ApiImportSource sourceType, String fileName) {
    String tmpPath;
    SpringAppDirUtils utils = new SpringAppDirUtils();
    
    // Build path based on import source type
    if (sourceType.equals(ApiImportSource.OPENAPI)) {
      // Create OpenAPI-specific import directory
      tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_OPENAPI_DIR + getTenantId()
          + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    } else /*if (sourceType.equals(ApiImportSource.POSTMAN))*/ {
      // Create Postman-specific import directory
      tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_POSTMAN_DIR + getTenantId()
          + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    }
    
    // Create directory and return file object
    File file = new File(tmpPath);
    file.mkdirs();
    return file;
  }

  /**
   * Creates a temporary path for API export operations.
   * <p>
   * Generates tenant-specific temporary paths for export processing.
   * <p>
   * Handles both directory and file path creation based on the provided file name.
   * <p>
   * @param fileName optional file name to include in the path
   * @return the created temporary path for export operations
   */
  public static File getExportTmpPath(String fileName) {
    SpringAppDirUtils utils = new SpringAppDirUtils();
    
    // Build export path with tenant isolation and unique identifier
    String tmpPath =
        utils.getTmpDir() + TesterConstant.EXPORT_OPENAPI_DIR + getTenantId() + File.separator
            + randomUUID() + File.separator + nullSafe(fileName, "");
    
    // Create file object and ensure parent directory exists
    File file = new File(tmpPath);
    if (file.isDirectory()) {
      // Create directory if path represents a directory
      file.mkdirs();
    } else {
      // Create parent directory if path represents a file
      file.getParentFile().mkdirs();
    }
    return file;
  }


}
