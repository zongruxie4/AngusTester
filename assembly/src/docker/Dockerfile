# 使用多阶段构建减小镜像体积
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY @hump.name@-@editionName@-@project.version@ .
RUN chmod +x startup-tester.sh && \
    chmod +x shutdown-tester.sh && \
    # rm -rf tmp logs data  # 删除临时/日志/数据目录

# 最终镜像
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="AngusTester Team"

# 设置环境变量 (默认端口8901)
ENV TESTER_PORT=8901 \
    APP_HOME=/opt/angustester

# 创建目录结构
RUN mkdir -p ${APP_HOME} && \
    mkdir -p ${APP_HOME}/conf && \
    mkdir -p ${APP_HOME}/plugins && \
    adduser -D angus

# 复制应用文件
COPY --from=builder --chown=angus:angus /app ${APP_HOME}

# 设置挂载点 (允许用户映射的目录)
VOLUME ["${APP_HOME}/conf", "${APP_HOME}/data", "${APP_HOME}/logs", "${APP_HOME}/plugins", "${APP_HOME}/tmp"]

# 设置工作目录和用户
WORKDIR ${APP_HOME}
USER angus

# 暴露端口 (可配置)
EXPOSE ${TESTER_PORT}

# 健康检查 (使用可配置端口)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -qO- http://localhost:${TESTER_PORT}/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["./startup-tester.sh"]
