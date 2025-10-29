# SDR Waveform Streaming Implementation

## Summary

I've successfully implemented the backend infrastructure to stream SDR waveform (IQ) data to the frontend in real-time. The implementation follows the same pattern as the existing audio streaming feature.

## What Has Been Implemented

### Backend Components

1. **WaveformWebSocket.java**
   - WebSocket endpoint for waveform data streaming
   - Located at: `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WaveformWebSocket.java`
   - Handles client connections and sends binary waveform data

2. **WebStreamWaveformBroadcaster.java**
   - Manages multiple WebSocket clients
   - Receives ComplexSamples (IQ data) and broadcasts to all connected clients
   - Implements decimation (sends every 100th sample) to reduce bandwidth
   - Limits packet size to 512 samples maximum
   - Binary format: [4 bytes: sample count][4 bytes: timestamp][float32 I, float32 Q] * N samples

3. **WaveformSampleTap.java**
   - Listener that taps into tuner sample streams
   - Converts INativeBuffer to ComplexSamples
   - Rate limits processing (processes every 10th buffer)
   - Forwards samples to the waveform broadcaster

4. **WebStreamServer.java** (Enhanced)
   - Added waveform broadcaster alongside audio broadcaster
   - New WebSocket endpoint at `/waveform`
   - Automatically attaches waveform taps to all available tuners on startup
   - Methods to attach/detach waveform taps dynamically
   - Accepts TunerManager parameter to access tuners

5. **SDRTrunk.java** (Updated)
   - Now passes TunerManager to WebStreamServer constructor
   - Waveform streaming starts automatically with the web server

## How It Works

### Data Flow
```
SDR Tuner
    ↓
TunerController (broadcasts INativeBuffer)
    ↓
WaveformSampleTap (converts to ComplexSamples, rate limits)
    ↓
WebStreamWaveformBroadcaster (decimates, packetizes)
    ↓
WaveformWebSocket (sends to clients)
    ↓
Web Browser (WebSocket client)
```

### Performance Optimizations
- **Buffer-level rate limiting**: Only processes every 10th buffer
- **Sample decimation**: Sends every 100th sample (configurable)
- **Packet size limiting**: Maximum 512 samples per packet
- **Client count checking**: Only processes data when clients are connected

### WebSocket Endpoints
- `/audio` - Audio streaming (existing)
- `/waveform` - IQ waveform data streaming (new)

## What Needs to Be Done (Frontend)

To complete the implementation, you need to add frontend visualization. Here's what to add to the HTML/JavaScript in WebUIServlet.java:

### 1. Add Waveform Canvas to HTML
```html
<div class="waveform-container">
    <div class="waveform-label">IQ WAVEFORM</div>
    <canvas id="waveformCanvas" width="600" height="200"></canvas>
</div>
```

### 2. Add CSS Styling
```css
.waveform-container {
    background: #000;
    border: 2px solid #00ff00;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 20px;
    box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
}

.waveform-label {
    color: #00aa00;
    font-size: 0.8em;
    text-transform: uppercase;
    letter-spacing: 2px;
    margin-bottom: 8px;
    text-align: center;
}

#waveformCanvas {
    width: 100%;
    height: 200px;
    background: #000;
}
```

### 3. Add JavaScript WebSocket Connection and Visualization
```javascript
let waveformWs = null;
const waveformCanvas = document.getElementById('waveformCanvas');
const waveformCtx = waveformCanvas.getContext('2d');

function connectWaveform() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/waveform`;
    
    waveformWs = new WebSocket(wsUrl);
    waveformWs.binaryType = 'arraybuffer';
    
    waveformWs.onopen = () => {
        console.log('Waveform WebSocket connected');
    };
    
    waveformWs.onmessage = (event) => {
        drawWaveform(event.data);
    };
    
    waveformWs.onerror = (error) => {
        console.error('Waveform WebSocket error:', error);
    };
    
    waveformWs.onclose = () => {
        console.log('Waveform WebSocket closed');
    };
}

function drawWaveform(data) {
    const view = new DataView(data);
    const sampleCount = view.getInt32(0, true);
    const timestamp = view.getUint32(4, true);
    
    const width = waveformCanvas.width;
    const height = waveformCanvas.height;
    const centerY = height / 2;
    
    // Clear canvas
    waveformCtx.fillStyle = '#000';
    waveformCtx.fillRect(0, 0, width, height);
    
    // Draw IQ samples
    waveformCtx.strokeStyle = '#00ff00';
    waveformCtx.lineWidth = 1;
    
    // Draw I channel (in-phase)
    waveformCtx.beginPath();
    for (let i = 0; i < sampleCount; i++) {
        const offset = 8 + i * 8; // Skip header
        const iValue = view.getFloat32(offset, true);
        const x = (i / sampleCount) * width;
        const y = centerY - (iValue * centerY * 0.8);
        
        if (i === 0) {
            waveformCtx.moveTo(x, y);
        } else {
            waveformCtx.lineTo(x, y);
        }
    }
    waveformCtx.stroke();
    
    // Draw Q channel (quadrature) in different color
    waveformCtx.strokeStyle = '#ff00ff';
    waveformCtx.beginPath();
    for (let i = 0; i < sampleCount; i++) {
        const offset = 12 + i * 8; // Skip header + I value
        const qValue = view.getFloat32(offset, true);
        const x = (i / sampleCount) * width;
        const y = centerY - (qValue * centerY * 0.8);
        
        if (i === 0) {
            waveformCtx.moveTo(x, y);
        } else {
            waveformCtx.lineTo(x, y);
        }
    }
    waveformCtx.stroke();
    
    // Draw center line
    waveformCtx.strokeStyle = '#333';
    waveformCtx.beginPath();
    waveformCtx.moveTo(0, centerY);
    waveformCtx.lineTo(width, centerY);
    waveformCtx.stroke();
}

// Call this when connecting
function connect() {
    // ... existing audio connection code ...
    connectWaveform();
}
```

## Configuration Options

You can adjust these constants in the source files:

### WaveformSampleTap.java
- `SAMPLE_INTERVAL = 10` - Process every Nth buffer (higher = less CPU, lower update rate)

### WebStreamWaveformBroadcaster.java
- `DECIMATION_FACTOR = 100` - Send every Nth sample (higher = less bandwidth, lower resolution)
- `MAX_SAMPLES_PER_PACKET = 512` - Maximum samples per packet

## Testing

1. Build the project: `./gradlew build`
2. Start SDRTrunk
3. Navigate to `http://localhost:8080`
4. Click "Connect" to establish WebSocket connections
5. The waveform should start displaying IQ data from the active tuner

## Binary Protocol

Each waveform packet contains:
- Header (8 bytes):
  - 4 bytes: int32 sample count (little-endian)
  - 4 bytes: int32 timestamp lower 32 bits (little-endian)
- Sample data (8 bytes per sample):
  - 4 bytes: float32 I value (little-endian)
  - 4 bytes: float32 Q value (little-endian)

## Future Enhancements

Possible improvements:
- Waterfall/spectrogram display
- Constellation diagram (I vs Q plot)
- FFT/spectrum analyzer view
- Adjustable decimation factor from UI
- Multiple tuner selection
- Recording waveform data from browser
- Signal strength indicator

## Files Modified/Created

### Created
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WaveformWebSocket.java`
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamWaveformBroadcaster.java`
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WaveformSampleTap.java`

### Modified
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebStreamServer.java`
- `src/main/java/io/github/dsheirer/gui/SDRTrunk.java`

### To Modify (Frontend)
- `src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebUIServlet.java`

## Build Status

✅ The code compiles successfully
✅ No new dependencies required (uses existing Jetty WebSocket libraries)
✅ Backend infrastructure is complete and ready to use
