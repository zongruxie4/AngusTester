package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.core.utils.CoreUtils.randomUUID;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;

import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.spec.utils.FileUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

/**
 * Utility class for project data export file operations.
 * <p>
 * Provides file handling utilities for creating ZIP and TAR archives, organizing exported files,
 * and managing temporary paths.
 */
public class ProjectExportFileUtils {

  private static final String EXPORT_PROJECT_DIR = "/export/project/";

  /**
   * Creates a temporary directory path for project export operations.
   */
  @SneakyThrows
  public static File createTempExportDir(String projectName) {
    String tmpPath = new SpringAppDirUtils().getTmpDir() + EXPORT_PROJECT_DIR + getTenantId()
        + File.separator + projectName + "-" + randomUUID();
    File dir = new File(tmpPath);
    FileUtils.forceMkdir(dir);
    return dir;
  }

  /**
   * Creates a scripts subdirectory in the export directory.
   */
  @SneakyThrows
  public static File createScriptsDir(File exportDir) {
    File scriptsDir = new File(exportDir, "scripts");
    FileUtils.forceMkdir(scriptsDir);
    return scriptsDir;
  }

  /**
   * Writes JSON content to a file.
   */
  @SneakyThrows
  public static File writeJsonFile(File dir, String fileName, String jsonContent) {
    File jsonFile = new File(dir, fileName + ".json");
    FileUtils.writeStringToFile(jsonFile, jsonContent, StandardCharsets.UTF_8);
    return jsonFile;
  }

  /**
   * Writes YAML content to a file.
   */
  @SneakyThrows
  public static File writeYamlFile(File dir, String fileName, String yamlContent) {
    File yamlFile = new File(dir, fileName + ".yaml");
    FileUtils.writeStringToFile(yamlFile, yamlContent, StandardCharsets.UTF_8);
    return yamlFile;
  }

  /**
   * Creates a ZIP archive from a directory.
   */
  @SneakyThrows
  public static File createZipArchive(File sourceDir, File outputFile) {
    List<File> filesToCompress = getAllFiles(sourceDir);
    String[] filePaths = filesToCompress.stream()
        .map(File::getAbsolutePath)
        .toArray(String[]::new);
    String[] fileNames = filesToCompress.stream()
        .map(file -> sourceDir.toPath().relativize(file.toPath()).toString().replace("\\", "/"))
        .toArray(String[]::new);

    FileUtils.compress(filePaths, fileNames, outputFile.getAbsolutePath());
    return outputFile;
  }

  /**
   * Creates a TAR archive from a directory.
   */
  @SneakyThrows
  public static File createTarArchive(File sourceDir, File outputFile, boolean gzip) {
    try (FileOutputStream fos = new FileOutputStream(outputFile);
        TarArchiveOutputStream tarOs = createTarOutputStream(fos, gzip)) {

      List<File> filesToArchive = getAllFiles(sourceDir);
      for (File file : filesToArchive) {
        String entryName = sourceDir.toPath().relativize(file.toPath()).toString()
            .replace("\\", "/");
        TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
        tarOs.putArchiveEntry(entry);
        Files.copy(file.toPath(), tarOs);
        tarOs.closeArchiveEntry();
      }
    }
    return outputFile;
  }

  /**
   * Creates TarArchiveOutputStream, optionally with GZIP compression.
   */
  private static TarArchiveOutputStream createTarOutputStream(FileOutputStream fos, boolean gzip)
      throws IOException {
    if (gzip) {
      return new TarArchiveOutputStream(new GzipCompressorOutputStream(fos));
    } else {
      return new TarArchiveOutputStream(fos);
    }
  }

  /**
   * Gets all files recursively from a directory.
   */
  private static List<File> getAllFiles(File dir) {
    List<File> files = new ArrayList<>();
    if (dir.isDirectory()) {
      File[] children = dir.listFiles();
      if (children != null) {
        for (File child : children) {
          if (child.isDirectory()) {
            files.addAll(getAllFiles(child));
          } else {
            files.add(child);
          }
        }
      }
    } else if (dir.isFile()) {
      files.add(dir);
    }
    return files;
  }
}

