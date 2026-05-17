FROM eclipse-temurin:8-jre-alpine

ARG APP_JAR=sie-iidp-demo-start/target/sie-iidp-demo-start-1.0-SNAPSHOT.jar

ENV TZ=Asia/Shanghai \
    APP_HOME=/opt/iidp \
    JAVA_OPTS="-Xms512m -Xmx1024m"

RUN apk add --no-cache tzdata curl \
    && cp /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone \
    && mkdir -p /apps /config /logs ${APP_HOME}

WORKDIR ${APP_HOME}

COPY apps/ /apps/
COPY ${APP_JAR} /app.jar

EXPOSE 8060

ENTRYPOINT ["/bin/sh", "-c"]
CMD ["java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -Dspring.config.additional-location=file:/config/ -Xbootclasspath/a:/config -Dspring.output.ansi.enabled=always -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai -jar /app.jar"]
