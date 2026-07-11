FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

COPY pom.xml pom.xml
COPY AuthService/pom.xml AuthService/pom.xml

RUN mvn -pl AuthService dependency:go-offline -B

COPY AuthService/src AuthService/src

RUN mvn -pl AuthService clean package -DskipTests -Dspotless.skip=true -Dcheckstyle.skip=true -Dpmd.skip=true -Dspotbugs.skip=true -Ddependency-check.skip=true -B

FROM eclipse-temurin:21-jre-alpine AS runner

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /workspace/AuthService/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

ENV PORT=8080
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC \
               -XX:MaxRAMPercentage=75.0 \
               -XX:MinRAMPercentage=50.0 \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
