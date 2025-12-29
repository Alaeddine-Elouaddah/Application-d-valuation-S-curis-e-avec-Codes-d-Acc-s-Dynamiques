FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Install X11, VNC, and minimal dependencies for Ubuntu 24.04
RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    fluxbox \
    novnc \
    websockify \
    libgtk-3-0 \
    libxtst6 \
    libxrender1 \
    libxi6 \
    libgl1 \
    libglx0 \
    fontconfig \
    fonts-dejavu \
    fonts-liberation \
    wget \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p ~/.vnc \
    && echo "fluxbox" > ~/.vnc/xstartup \
    && chmod 755 ~/.vnc/xstartup

# Copy the fat JAR (all dependencies included)
COPY --from=builder /app/target/ProjetQcm-1.0-SNAPSHOT.jar /app/app.jar

# Copy startup script
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 5900 6080

CMD ["/app/start.sh"]