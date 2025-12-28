FROM maven:3.9.4-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    xvfb \
    x11vnc \
    fluxbox \
    libgtk-3-0 \
    libx11-6 \
    libxext6 \
    libxrender1 \
    libxrandr2 \
    libxinerama1 \
    libxcursor1 \
    libxi6 \
    libxtst6 \
    libxcomposite1 \
    libxdamage1 \
    libxkbcommon0 \
    fontconfig \
    fonts-dejavu \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/app.jar /app/app.jar
COPY --from=builder /app/target/lib /app/lib

RUN printf '#!/bin/bash\n\
set -e\n\
\n\
export DISPLAY=:99\n\
export LIBGL_ALWAYS_INDIRECT=1\n\
\n\
echo \"Starting Xvfb...\"\n\
Xvfb :99 -screen 0 1280x800x24 &\n\
sleep 2\n\
\n\
echo \"Starting window manager...\"\n\
fluxbox &\n\
sleep 1\n\
\n\
echo \"Starting VNC server...\"\n\
x11vnc -display :99 -forever -shared -nopw -listen 0.0.0.0 &\n\
sleep 1\n\
\n\
echo \"Launching JavaFX app...\"\n\
exec java \\\n\
  -Djavafx.platform=linux \\\n\
  -Dprism.order=sw \\\n\
  --module-path /app/lib \\\n\
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.media \\\n\
  -cp /app/app.jar \\\n\
  com.project.projetqcm.Launcher\n' > /app/start.sh \
&& chmod +x /app/start.sh

EXPOSE 5900
ENTRYPOINT ["/app/start.sh"]

