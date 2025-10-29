# Web Streaming Updates - Channel Name and Network Access

## Changes Made

### 1. Channel Name Display on Web UI

#### Backend Changes:

**AudioSegment.java**
- Added `mChannelName` field
- Added `setChannelName(String)` method
- Added `getChannelName()` method

**AbstractAudioModule.java**
- Added `mChannelName` field
- Added `setChannelName(String)` method
- Updated `getAudioSegment()` to call `setChannelName()` on new audio segments

**DecoderFactory.java**
- Added `setChannelNameOnAudioModules()` helper method
- Updated `getModules()` to call `setChannelNameOnAudioModules()` with channel name

**WebStreamAudioBroadcaster.java**
- Added `Gson` dependency for JSON serialization
- Added `createMetadata()` method to generate JSON metadata
- Updated `receive()` to send metadata before audio data
- Metadata includes: channelName, timestamp, duration, encrypted, timeslot

**AudioWebSocket.java**
- Added `sendMetadata(String)` method to send text messages

#### Frontend Changes:

**WebUIServlet.java (HTML/JavaScript)**
- Added "Channel" field to info panel
- Added `currentChannelName` variable
- Added `processMetadata()` function to parse JSON metadata
- Updated WebSocket message handler to process both text (metadata) and binary (audio) messages
- Channel name is now displayed and updated in real-time

### 2. Listen on All Network Interfaces

**WebStreamServer.java**
- Added `connector.setHost("0.0.0.0")` to listen on all IP addresses
- Updated log message to indicate "listening on all interfaces"

This allows the web stream to be accessed from:
- `http://localhost:8080` (local machine)
- `http://192.168.1.x:8080` (from other devices on LAN)
- `http://YOUR_IP:8080` (from any network interface)

## How It Works

### Data Flow:

```
Channel → DecoderFactory (sets channel name)
   ↓
AbstractAudioModule (stores channel name)
   ↓
AudioSegment (receives channel name when created)
   ↓
WebStreamAudioBroadcaster (extracts metadata + audio)
   ↓
AudioWebSocket (sends to browser)
   ↓  ↓
  Text   Binary
  (JSON) (PCM Audio)
   ↓      ↓
Web Browser (displays channel name + plays audio)
```

### Metadata Format (JSON):

```json
{
  "channelName": "Police Dispatch",
  "timestamp": 1698765432000,
  "duration": 250,
  "encrypted": false,
  "timeslot": 0
}
```

## Testing

Build tested successfully:
```bash
export JAVA_HOME=/tmp/jdk-24.0.1-full
export PATH=$JAVA_HOME/bin:$PATH
./gradlew compileJava
# BUILD SUCCESSFUL
```

## Usage

1. **Start SDRTrunk**: `./run.sh`
2. **Access from same computer**: `http://localhost:8080`
3. **Access from another device**: 
   - Find your IP: `ip addr` or `ifconfig`
   - Navigate to: `http://YOUR_IP:8080`
4. **Watch the channel name update** as different channels become active

## Files Modified

1. `src/main/java/io/github/dsheirer/audio/AudioSegment.java`
2. `src/main/java/io/github/dsheirer/audio/AbstractAudioModule.java`
3. `src/main/java/io/github/dsheirer/module/decode/DecoderFactory.java`
4. `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamServer.java`
5. `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamAudioBroadcaster.java`
6. `src/main/java/io/github/dsheirer/audio/broadcast/webstream/AudioWebSocket.java`
7. `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebUIServlet.java`

## UI Screenshot Description

The web UI now shows:
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
│  Channel: Police Dispatch ← NEW!        │
│  Sample Rate: 8000 Hz                   │
│  Channels: Mono                         │
│  Format: 16-bit PCM                     │
│  Packets Received: 1234                 │
└─────────────────────────────────────────┘
```

The channel name updates in real-time as audio from different channels is decoded.
