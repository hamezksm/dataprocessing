# Multi-stage Dockerfile: build with Maven, run with slim JDK
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy pom and download dependencies (leverages layer cache)
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml -q -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -f pom.xml -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy jar from build stage
COPY --from=build /workspace/target/dataprocessing-0.0.1-SNAPSHOT.jar ./app.jar

# Create data dir for generated files
RUN mkdir -p /app/data
VOLUME ["/app/data"]

# Increase heap size for processing large Excel files (1M records)
ENV JAVA_OPTS="-Xms512m -Xmx2g"
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS

EXPOSE 8080

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
