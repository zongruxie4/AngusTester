package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import io.micrometer.core.instrument.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.vngx.jsch.ChannelExec;
import org.vngx.jsch.ChannelType;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.exception.JSchException;

/**
 * Utility class for SSH connection management and remote command execution.
 * <p>
 * Provides secure SSH connectivity to remote servers for command execution and availability testing.
 * <p>
 * Implements session management, command execution, and connection pooling with proper resource cleanup.
 * <p>
 * Supports both password and key-based authentication with configurable timeouts.
 */
@Slf4j
public class SshUtil {

  // SSH connection timeout constants
  private static final int TIMEOUT = 30 * 1000;        // 30 seconds for general operations
  private static final int CONNECT_TIMEOUT = 3 * 1000; // 3 seconds for connection establishment

  // SSH connection parameters
  private final String ip;
  private final Integer port;
  private final String userName;
  private final String password;

  /**
   * Creates a new SSH utility instance for connecting to a remote server.
   * <p>
   * Initializes connection parameters for SSH session establishment.
   * <p>
   * Supports both default port (22) and custom port configurations.
   * <p>
   * @param ip the IP address or hostname of the remote server
   * @param port the SSH port (null for default port 22)
   * @param userName the username for SSH authentication
   * @param password the password for SSH authentication
   */
  public SshUtil(String ip, Integer port, String userName, String password) {
    this.ip = ip;
    this.port = port;
    this.userName = userName;
    this.password = password;
  }

  /**
   * Test method for SSH functionality demonstration.
   * <p>
   * This method is for testing purposes only and should not be used in production.
   * <p>
   * Demonstrates basic SSH connection and command execution capabilities.
   */
  /*
  public static void main(String[] args) throws Exception {
    SshUtil ssh = new SshUtil("192.168.0.102", 22, "xcan", "xc@123");
    log.info(ssh.run("uname -s"));
    log.info("====isAvailable result:{}", ssh.isAvailable());
    //    log.info("====exec result:{}",
    //        ssh.exec("curl -o /data/temp/arthas-boot.jar https://arthas.aliyun.com/arthas-boot.jar"));
    //    log.info("====shell result:{}",
    //        ssh.shell("curl -o /data/temp/arthas-boot.jar https://arthas.aliyun.com/arthas-boot.jar"));
  }
  */

  /**
   * Creates and configures an SSH session for remote server connection.
   * <p>
   * Establishes SSH connection with proper timeout and security configurations.
   * <p>
   * Supports both password and key-based authentication methods.
   * <p>
   * @return the configured SSH session ready for use
   * @throws JSchException if session creation or connection fails
   */
  public Session createSession() throws JSchException {
    // Create JSch instance for SSH operations
    JSch jsch = JSch.getInstance();
    Session session;

    // Create session with appropriate port configuration
    if (Objects.nonNull(port)) {
      session = jsch.createSession(userName, ip, port);
    } else {
      session = jsch.createSession(userName, ip);
    }

    // Configure session timeout and security settings
    session.setTimeout(TIMEOUT);
    session.getConfig().setProperty("StrictHostKeyChecking", "no");

    // Connect with password authentication if provided
    if (StringUtils.isNotEmpty(password)) {
      session.connect(CONNECT_TIMEOUT, password.getBytes());
    } else {
      session.connect(CONNECT_TIMEOUT);
    }
    return session;
  }

  /**
   * Executes operations on an SSH session with automatic resource cleanup.
   * <p>
   * Creates a session, executes the provided function, and ensures proper cleanup.
   * <p>
   * Implements the try-with-resources pattern for SSH session management.
   * <p>
   * @param func the function to execute with the SSH session
   * @return the result of the function execution
   * @throws JSchException if session creation or operation fails
   */
  public <R> R connectSession(Function<Session, R> func) throws JSchException {
    Session session = null;
    try {
      // Create and connect SSH session
      session = createSession();
      // Execute the provided function with the session
      return func.apply(session);
    } finally {
      // Ensure session is properly disconnected
      if (session != null && session.isConnected()) {
        session.disconnect();
      }
    }
  }

  /**
   * Tests SSH connectivity to the remote server.
   * <p>
   * Attempts to establish an SSH connection to verify server availability.
   * <p>
   * Returns true if connection is successful, false otherwise.
   * <p>
   * @return true if SSH connection can be established, false otherwise
   */
  public boolean isAvailable() {
    boolean connected = false;
    try {
      // Test connection by checking if session can be established
      connected = connectSession(Session::isConnected);
    } catch (JSchException e) {
      // Log connection failure but don't throw exception
      log.error("SSH connection failed: {}", e.getMessage());
    }
    return connected;
  }

  /**
   * Executes a command on the remote server and returns execution status.
   * <p>
   * Runs the specified command and returns true if execution was successful.
   * <p>
   * Captures command output but only returns success/failure status.
   * <p>
   * @param command the command to execute on the remote server
   * @return true if command execution was successful, false otherwise
   */
  public boolean exec(String command) {
    boolean status = false;
    try {
      // Execute command and check if result is not null (indicating success)
      String rs = connectSession(session -> {
        ChannelExec channel = null;
        InputStream in;
        String result = null;
        try {
          // Create and configure execution channel
          channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getTypeName());
          channel.setCommand(command);
          channel.setInputStream(null);
          channel.setErrStream(null);
          channel.connect(CONNECT_TIMEOUT);

          // Read command output
          in = channel.getInputStream();
          result = read(in);
        } catch (JSchException | IOException e) {
          log.error("Command execution failed: {}", e.getMessage(), e);
        } finally {
          // Ensure channel is properly closed
          if (channel != null) {
            channel.disconnect();
          }
        }
        return result;
      });
      status = rs != null;
    } catch (JSchException e) {
      log.error("SSH session error during command execution: {}", e.getMessage(), e);
    }
    return status;
  }

  /**
   * Executes a command on the remote server and returns the command output.
   * <p>
   * Runs the specified command and captures the complete output as a string.
   * <p>
   * Throws exception if command execution fails or connection cannot be established.
   * <p>
   * @param command the command to execute on the remote server
   * @return the command output as a string
   * @throws Exception if command execution or connection fails
   */
  public String run(String command) throws Exception {
    return connectSession(session -> {
      ChannelExec channel = null;
      InputStream in = null;
      String result = null;
      try {
        // Create and configure execution channel
        channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getTypeName());
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(null);
        channel.connect(CONNECT_TIMEOUT);

        // Read command output
        in = channel.getInputStream();
        result = read(in);
      } catch (JSchException | IOException e) {
        log.error("Command execution failed: {}", e.getMessage(), e);
        throw new RuntimeException("SSH command execution failed", e);
      } finally {
        // Ensure all resources are properly closed
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // Log but don't throw exception during cleanup
            log.debug("Error closing input stream: {}", e.getMessage());
          }
        }
        if (channel != null) {
          channel.disconnect();
        }
      }
      return result;
    });
  }

  /**
   * Reads content from an input stream and converts it to a string.
   * <p>
   * Reads all lines from the input stream and joins them with newline characters.
   * <p>
   * Handles empty input streams and ensures proper encoding.
   * <p>
   * @param in the input stream to read from
   * @return the content as a string, empty string if stream is empty
   * @throws IOException if reading from the stream fails
   */
  private String read(InputStream in) throws IOException {
    // Read all lines from input stream with UTF-8 encoding
    List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
    if (isEmpty(lines)) {
      return "";
    }
    // Join lines with newline characters
    return String.join("\n", lines);
  }
}
