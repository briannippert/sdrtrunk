# SDRTrunk Web Streaming - Quick Start Guide

## What Was Added

A complete web-based audio streaming solution that lets you listen to SDRTrunk's decoded audio in any web browser!

## Prerequisites

**Java 24 Required** - This project requires Java 24. 

### Option 1: Use the provided scripts (easiest)
The `build.sh` and `run.sh` scripts will automatically find Java 24 if it's installed in `/tmp` or your home directory.

### Option 2: Install Java 24 manually
Download from: https://download.bell-sw.com/java/24.0.1+11/bellsoft-jdk24.0.1+11-linux-amd64-full.tar.gz

```bash
wget https://download.bell-sw.com/java/24.0.1+11/bellsoft-jdk24.0.1+11-linux-amd64-full.tar.gz
tar -xzf bellsoft-jdk24.0.1+11-linux-amd64-full.tar.gz
export JAVA_HOME=$PWD/jdk-24.0.1-full
export PATH=$JAVA_HOME/bin:$PATH
```

## Quick Start (3 Steps)

### 1. Build SDRTrunk
```bash
./build.sh
```
Or manually:
```bash
export JAVA_HOME=/path/to/jdk-24.0.1-full
export PATH=$JAVA_HOME/bin:$PATH
./gradlew build -x test
```

### 2. Start SDRTrunk
```bash
./run.sh
```
Or manually:
```bash
export JAVA_HOME=/path/to/jdk-24.0.1-full
export PATH=$JAVA_HOME/bin:$PATH
./gradlew run
```

The web server automatically starts on port **8080** when SDRTrunk launches.

### 3. Open Your Browser
Navigate to: **http://localhost:8080**

Then:
1. Click the **Connect** button
2. Click the **Play** button
3. Enjoy the audio!

## Features at a Glance

✅ **Real-time Streaming** - WebSocket-based, low-latency audio  
✅ **Beautiful UI** - Modern design with gradient backgrounds  
✅ **Audio Visualizer** - 20-bar real-time waveform display  
✅ **Multi-Client** - Multiple browsers can connect simultaneously  
✅ **No Configuration** - Works out of the box  
✅ **Remote Access** - Access from any device on your network  

## Access from Other Devices

1. Find your computer's IP address:
   - Windows: `ipconfig`
   - Linux/Mac: `ifconfig` or `ip addr`

2. On another device, open browser and go to:
   ```
   http://YOUR_IP_ADDRESS:8080
   ```
   Example: `http://192.168.1.100:8080`

## UI Overview

```
┌─────────────────────────────────────────┐
│         🎵 SDRTrunk                     │
│        Live Audio Stream                │
├─────────────────────────────────────────┤
│     [Connected] (status indicator)      │
├─────────────────────────────────────────┤
│  [Connect]  [Play]  [Stop]              │
├─────────────────────────────────────────┤
│  ▂▃▅▇█▇▅▃▂  (audio visualizer)          │
├─────────────────────────────────────────┤
│  Sample Rate: 8000 Hz                   │
│  Channels: Mono                         │
│  Format: 16-bit PCM                     │
│  Packets Received: 1234                 │
└─────────────────────────────────────────┘
```

## Technical Specs

| Property | Value |
|----------|-------|
| Port | 8080 |
| Protocol | WebSocket (ws://) |
| Sample Rate | 8000 Hz |
| Audio Format | 16-bit PCM, Mono |
| Latency | ~200-500ms |

## Browser Support

✅ Chrome/Chromium 90+  
✅ Firefox 88+  
✅ Safari 14+  
✅ Edge 90+  
✅ Mobile browsers (iOS Safari, Chrome Android)  

## Troubleshooting

### Can't connect?
- Make sure SDRTrunk is running
- Check firewall isn't blocking port 8080
- Try `http://127.0.0.1:8080` instead

### No audio?
- Click the **Play** button (not just Connect)
- Check browser permissions for audio playback
- Ensure SDRTrunk is receiving/decoding audio

### High latency?
- Reduce number of connected clients
- Check CPU usage in SDRTrunk
- Use a wired network connection

## Architecture Diagram

```
┌──────────────────┐
│   SDRTrunk GUI   │
│  (Desktop App)   │
└────────┬─────────┘
         │
         │ Audio Segments
         ↓
┌──────────────────┐
│ Channel Manager  │
└────────┬─────────┘
         │
         ↓
┌──────────────────────┐
│  WebStreamServer     │ ←─── Jetty HTTP Server
│  (Port 8080)         │      + WebSocket
└────────┬─────────────┘
         │
         ↓
┌──────────────────────┐
│ AudioBroadcaster     │ ←─── Manages clients
└───┬────┬────┬────────┘      Converts audio
    │    │    │
    ↓    ↓    ↓
  [WS] [WS] [WS]  ←─────────── WebSocket connections
    │    │    │
    ↓    ↓    ↓
 [🌐] [🌐] [🌐]  ←─────────── Web browsers
```

## Files Created

**Java Backend:**
- `WebStreamServer.java` - Main server
- `WebStreamAudioBroadcaster.java` - Client manager
- `AudioWebSocket.java` - WebSocket handler
- `WebUIServlet.java` - Web UI server

**Documentation:**
- `WEB_STREAMING.md` - Full documentation
- `IMPLEMENTATION_SUMMARY.md` - Developer guide
- `CHANGES.txt` - Change log
- `WEB_STREAMING_QUICKSTART.md` - This file

## Next Steps

After you have it running:

1. **Customize the port**: Edit `SDRTrunk.java`, line ~229
2. **Secure it**: Add firewall rules to restrict access
3. **Extend it**: Add features like volume control, metadata display

## Need Help?

Check the detailed documentation:
- User Guide: `WEB_STREAMING.md`
- Developer Info: `IMPLEMENTATION_SUMMARY.md`
- Change Summary: `CHANGES.txt`

Enjoy streaming! 🎵
