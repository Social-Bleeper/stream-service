# ---- Base JDK 17 ----
FROM eclipse-temurin:17-jdk AS base
ENV APP_PATH=bleeper/stream-service


# ---- Dependencies ----
FROM base AS dependencies
ENV MAVEN_VERSION="3.9.10"
ENV M2_HOME=/opt/maven
ENV PATH=$M2_HOME/bin/:$PATH
RUN cd /opt \
    && wget https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && tar -zxvf apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && mv apache-maven-${MAVEN_VERSION} maven
# copy Maven dependencies & sources
WORKDIR /${APP_PATH}
COPY . .
RUN mvn clean package


# ---- Release ----
FROM base AS release
# copy target .jar file
COPY --from=dependencies /${APP_PATH}/target/*.jar bleeper-stream-service.jar
EXPOSE 8083

# start the application
ENTRYPOINT ["java", "-jar", "/bleeper-stream-service.jar"]