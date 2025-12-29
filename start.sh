#!/bin/sh
set -e

# ===============================
# Configuration
# ===============================
DISPLAY_NUM=:99
SCREEN_RES=1280x800x24
VNC_PORT=5900
NOVNC_PORT=6080

# ===============================
# Start virtual X display
# ===============================
echo "Starting Xvfb on display ${DISPLAY_NUM}..."
Xvfb ${DISPLAY_NUM} -screen 0 ${SCREEN_RES} &
export DISPLAY=${DISPLAY_NUM}

sleep 2

# ===============================
# Start window manager
# ===============================
echo "Starting Fluxbox..."
fluxbox &

sleep 2

# ===============================
# Start VNC server
# ===============================
echo "Starting x11vnc on port ${VNC_PORT}..."
x11vnc \
  -display ${DISPLAY} \
  -forever \
  -nopw \
  -shared \
  -rfbport ${VNC_PORT} &

# ===============================
# Start noVNC (Web VNC)
# ===============================
echo "Starting noVNC on port ${NOVNC_PORT}..."
websockify \
  --web=/usr/share/novnc/ \
  ${NOVNC_PORT} localhost:${VNC_PORT} &

echo "Starting Java application..."
exec java -jar /app/app.jar
