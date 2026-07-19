# --- build stage: compile and package the jar ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -q clean package -DskipTests

# --- run stage: small JRE image that runs the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/newsletter-0.0.1-SNAPSHOT.jar app.jar
# the app reads SPRING_DATASOURCE_*, JWT_SECRET and PORT from the environment
ENTRYPOINT ["java", "-jar", "app.jar"]
