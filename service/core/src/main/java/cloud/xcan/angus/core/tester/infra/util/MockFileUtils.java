package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.core.utils.CoreUtils.randomUUID;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import java.io.File;

/**
 * Utility class for mock service file operations and path management.
 * <p>
 * Provides file path utilities for import and export operations of mock services.
 * <p>
 * Implements temporary directory creation and file path validation for mock data.
 * <p>
 * Supports tenant-specific file isolation and secure file handling.
 */
public class MockFileUtils {

  /**
   * Checks if a file path represents an Angus specification file.
   * <p>
   * Validates if the file path contains both "specification" and "angus/" patterns.
   * <p>
   * Used to identify Angus API specification files during import operations.
   * <p>
   * @param file the file path to check
   * @return true if the file is an Angus specification file, false otherwise
   */
  public static boolean isAngusFile(String file) {
    // Check if file path contains both required patterns for Angus specification
    return file != null && file.contains("specification") && file.contains("angus/");
  }

  /**
   * Creates a temporary directory path for mock service import operations.
   * <p>
   * Generates a unique temporary directory with tenant isolation for import processing.
   * <p>
   * Ensures proper directory creation and tenant-specific file organization.
   * <p>
   * @param fileName optional file name to include in the path
   * @return the created temporary directory for import operations
   */
  public static File getImportTmpPath(String fileName) {
    // Create Spring application directory utility
    SpringAppDirUtils utils = new SpringAppDirUtils();
    
    // Build temporary path with tenant isolation and unique identifier
    String tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_MOCK_SERVICE_DIR + getTenantId()
        + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    
    // Create directory and return file object
    File file = new File(tmpPath);
    file.mkdirs();
    return file;
  }

  /**
   * Creates a temporary path for mock service export operations.
   * <p>
   * Generates a unique temporary path with tenant isolation for export processing.
   * <p>
   * Handles both directory and file path creation based on the provided file name.
   * <p>
   * @param fileName optional file name to include in the path
   * @return the created temporary path for export operations
   */
  public static File getExportTmpPath(String fileName) {
    // Create Spring application directory utility
    SpringAppDirUtils utils = new SpringAppDirUtils();
    
    // Build temporary path with tenant isolation and unique identifier
    String tmpPath = utils.getTmpDir() + TesterConstant.IMPORT_MOCK_SERVICE_DIR + getTenantId()
        + File.separator + randomUUID() + File.separator + nullSafe(fileName, "");
    
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
