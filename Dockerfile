FROM eclipse-temurin:17-jre AS builder
WORKDIR /application
ARG JAR_FILE=target/task-management-system-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17-jre
WORKDIR /application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
EXPOSE 8181
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
#, "--spring.profiles.active=prod"