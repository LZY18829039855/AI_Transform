FROM cd-docker-hub.szxy5.artifactory.cd-cloud-artifact.tools.huawei.com/coretool/centos-j8:1.0-ssh
ENV LC_ALL en_US.UTF-8
ENV TZ 'Asia/Shanghai'
MAINTAINER <liutanrong@huawei.com>

# 设置工作目录
WORKDIR /app

COPY target/ai-transform-1.0.0.jar app.jar


# 创建日志目录
RUN mkdir -p /app/logs

RUN chmod +r /app/app.jar

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]