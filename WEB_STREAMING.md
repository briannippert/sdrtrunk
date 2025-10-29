# SDRTrunk Web Audio Streaming

## Overview

SDRTrunk now includes a built-in web server that allows you to stream audio from the SDR backend directly to your web browser. This feature provides real-time audio streaming with a modern web-based user interface.

## Features

- **Real-time Audio Streaming**: Stream decoded audio from SDRTrunk to any web browser
- **WebSocket-based**: Low-latency streaming using WebSocket technology
- **Modern Web UI**: Clean, responsive interface with:
  - Audio visualizer showing real-time waveform
  - Connection status indicator
  - Play/Stop controls
  - Packet counter
  - Audio format information
- **Cross-platform**: Works on any device with a modern web browser

## How It Works

1. **Web Server**: An embedded Jetty web server starts automatically when SDRTrunk launches
2. **Audio Pipeline**: Audio segments from the channel processing manager are forwarded to the web stream server
3. **WebSocket Streaming**: Connected browsers receive audio data in real-time via WebSocket
4. **Browser Playback**: The Web Audio API plays the streamed audio with minimal latency

## Usage

### Starting the Web Server

The web server starts automatically when you launch SDRTrunk on port **8080**.

### Accessing the Web UI

1. Open your web browser
2. Navigate to: `http://localhost:8080`
3. Click **Connect** to establish WebSocket connection
4. Click **Play** to start audio playback

### Accessing from Other Devices

To access the stream from other devices on your network:

1. Find your computer's IP address (e.g., `192.168.1.100`)
2. On the remote device, navigate to: `http://YOUR_IP:8080`
3. Follow the same connection steps

## Technical Details

### Audio Format
- **Sample Rate**: 8000 Hz
- **Channels**: Mono
- **Format**: 16-bit PCM (converted from 32-bit float)
- **Encoding**: Little-endian

### Architecture

```
SDRTrunk Audio Pipeline
  ↓
Channel Processing Manager
  ↓
WebStreamServer (Listener<AudioSegment>)
  ↓
WebStreamAudioBroadcaster
  ↓
AudioWebSocket (per client)
  ↓
Web Browser (Web Audio API)
```

### Components

1. **WebStreamServer**: Main server class that manages the Jetty server and WebSocket endpoints
2. **WebStreamAudioBroadcaster**: Manages multiple WebSocket clients and broadcasts audio to all connected clients
3. **AudioWebSocket**: Handles individual WebSocket connections
4. **WebUIServlet**: Serves the HTML/CSS/JavaScript user interface

### Port Configuration

The default port is **8080**. To change it, modify the port parameter in `SDRTrunk.java`:

```java
mWebStreamServer = new WebStreamServer(8080); // Change 8080 to your desired port
```

## Browser Compatibility

The web UI requires a modern browser with support for:
- WebSocket API
- Web Audio API
- ES6+ JavaScript

Tested browsers:
- Chrome/Chromium 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Troubleshooting

### "Connection Error" in Web UI
- Ensure SDRTrunk is running
- Check that port 8080 is not blocked by firewall
- Verify the URL is correct

### No Audio Playing
- Click the **Play** button after connecting
- Check browser console for errors
- Ensure audio is being decoded in SDRTrunk

### High Latency
- Close other applications using network bandwidth
- Reduce the number of simultaneous clients
- Check CPU usage in SDRTrunk

## Security Considerations

- The web server runs on HTTP (not HTTPS) by default
- No authentication is implemented
- Only run on trusted networks
- Consider using a firewall to restrict access if needed

## Future Enhancements

Potential improvements for future versions:
- HTTPS support
- Authentication
- Multiple audio channel selection
- Volume control
- Recording from browser
- Metadata display (talkgroup, frequency, etc.)
- Configurable port via user preferences

## Code Files

The web streaming functionality is implemented in the following files:

- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamServer.java`
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamAudioBroadcaster.java`
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/AudioWebSocket.java`
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebUIServlet.java`

Integration is handled in:
- `src/main/java/io/github/dsheirer/gui/SDRTrunk.java`

Dependencies added to:
- `build.gradle`
