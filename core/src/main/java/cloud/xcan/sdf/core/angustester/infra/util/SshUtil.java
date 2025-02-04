package cloud.xcan.sdf.core.angustester.infra.util;

import io.micrometer.core.instrument.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.vngx.jsch.ChannelExec;
import org.vngx.jsch.ChannelType;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.exception.JSchException;

/**
 * @author wjl
 */
@Slf4j
public class SshUtil {

  private static final int TIMEOUT = 30 * 1000;
  private static final int CONNECT_TIMEOUT = 3 * 1000;

  private final String ip;
  private final Integer port;
  private final String userName;
  private final String password;

  public SshUtil(String ip, Integer port, String userName, String password) {
    this.ip = ip;
    this.port = port;
    this.userName = userName;
    this.password = password;
  }

  public static void main(String[] args) throws Exception {
    SshUtil ssh = new SshUtil("192.168.0.102", 22, "xcan", "xc@123");
    log.info(ssh.run("uname -s"));
    log.info("====isAvailable result:{}", ssh.isAvailable());
    //    log.info("====exec result:{}",
    //        ssh.exec("curl -o /data/temp/arthas-boot.jar https://arthas.aliyun.com/arthas-boot.jar"));
    //    log.info("====shell result:{}",
    //        ssh.shell("curl -o /data/temp/arthas-boot.jar https://arthas.aliyun.com/arthas-boot.jar"));
  }

  /**
   * create ssh session
   */
  public Session createSession() throws JSchException {
    JSch jsch = JSch.getInstance();
    Session session;
    if (Objects.nonNull(port)) {
      session = jsch.createSession(userName, ip, port);
    } else {
      session = jsch.createSession(userName, ip);
    }
    session.setTimeout(TIMEOUT);
    session.getConfig().setProperty("StrictHostKeyChecking", "no");
    if (StringUtils.isNotEmpty(password)) {
      session.connect(CONNECT_TIMEOUT, password.getBytes());
    } else {
      session.connect(CONNECT_TIMEOUT);
    }
    return session;
  }

  /**
   * execute session related actions
   */
  public <R> R connectSession(Function<Session, R> func) throws JSchException {
    Session session = null;
    try {
      session = createSession();
      return func.apply(session);
    } finally {
      if (session != null && session.isConnected()) {
        session.disconnect();
      }
    }
  }

  /**
   * Use the SSH command to connect to the specified server to test whether it is available
   */
  public boolean isAvailable() {
    boolean connected = false;
    try {
      connected = connectSession(Session::isConnected);
    } catch (JSchException e) {
      log.error(e.getMessage());
    }
    return connected;
  }

  public boolean exec(String command) {
    boolean status = false;
    try {
      String rs = connectSession(session -> {
        ChannelExec channel = null;
        InputStream in;
        String result = null;
        try {
          channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getTypeName());
          channel.setCommand(command);
          channel.setInputStream(null);
          channel.setErrStream(null);
          channel.connect(CONNECT_TIMEOUT);
          in = channel.getInputStream();
          result = read(in);
        } catch (JSchException | IOException e) {
          log.error(e.getMessage(), e);
        } finally {
          if (channel != null) {
            channel.disconnect();
          }
        }
        return result;
      });
      status = rs != null;
    } catch (JSchException e) {
      log.error(e.getMessage(), e);
    }
    return status;
  }

  public String run(String command) throws Exception {
    return connectSession(session -> {
      ChannelExec channel = null;
      InputStream in = null;
      String result = null;
      try {
        channel = (ChannelExec) session.openChannel(ChannelType.EXEC.getTypeName());
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setErrStream(null);
        channel.connect(CONNECT_TIMEOUT);
        in = channel.getInputStream();
        result = read(in);
      } catch (JSchException | IOException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e.getCause());
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // NOOP
          }
        }
        if (channel != null) {
          channel.disconnect();
        }
      }
      return result;
    });
  }

  private String read(InputStream in) throws IOException {
    List<String> lines = IOUtils.readLines(in, StandardCharsets.UTF_8);
    if (CollectionUtils.isEmpty(lines)) {
      return "";
    }
    return String.join("\n", lines);
  }
}
