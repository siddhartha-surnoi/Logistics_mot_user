FROM eclipse-temurin:21-jre-alpine
WORKDIR /opt/server

# Create group and user (Debian style)
RUN addgroup -S logistics && adduser -S logistics -G logistics

COPY ./target/Logistics-0.0.1-SNAPSHOT.jar /opt/server/logistics.jar

USER logistics
CMD ["java", "-jar", "logistics.jar"]