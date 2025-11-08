# Dockerfile for iap-gateway-service
# Requires JAR to be built locally first: ./gradlew clean build
# This approach avoids GitHub authentication issues during Docker build

FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user for security and logs directory
RUN useradd -m -u 1001 appuser && \
    mkdir -p /app/logs && \
    chown -R appuser:appuser /app && \
    chmod 755 /app/logs

# Copy pre-built JAR file
COPY --chown=appuser:appuser build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8089

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8089/actuator/health || exit 1

# JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Run application with docker profile
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar app.jar"]
