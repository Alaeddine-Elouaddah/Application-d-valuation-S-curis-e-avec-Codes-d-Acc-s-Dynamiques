#!/bin/bash
set -e

# Start Xvfb
Xvfb :0 -screen 0 1024x768x16 &
export DISPLAY=:0

# Start Fluxbox (window manager)
fluxbox &

# Start x11vnc
x11vnc -display :0 -forever -shared -rfbport 5900 -nopw &

# Start noVNC (websockify)
/usr/share/novnc/utils/launch.sh --vnc localhost:5900 --listen 8080 &

# Start the Java application
java -jar app.jar
