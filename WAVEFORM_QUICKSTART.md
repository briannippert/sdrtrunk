# SDR Waveform Streaming - Quick Start

## What Was Built

A complete backend system to stream raw IQ (In-phase/Quadrature) waveform data from SDR tuners to web browsers in real-time via WebSockets.

## Testing the Implementation

### 1. Build the Project
```bash
cd /home/brian/sdrtrunk
./gradlew build
```

### 2. Start SDRTrunk
```bash
./run.sh
```

### 3. Test the Waveform Stream

**Option A: Use the Test Page**
Open `waveform_test.html` in your browser:
```bash
# If SDRTrunk is running on localhost:8080
firefox waveform_test.html
# or
google-chrome waveform_test.html
```
Click "Connect" to start receiving waveform data.

**Option B: Use Browser Developer Console**
1. Open http://localhost:8080
2. Open Developer Console (F12)
3. Paste this code:
```javascript
const ws = new WebSocket('ws://localhost:8080/waveform');
ws.binaryType = 'arraybuffer';

ws.onopen = () => console.log('Waveform connected!');
ws.onmessage = (e) => {
    const view = new DataView(e.data);
    const count = view.getInt32(0, true);
    const timestamp = view.getUint32(4, true);
    const firstI = view.getFloat32(8, true);
    const firstQ = view.getFloat32(12, true);
    console.log(`Samples: ${count}, First I: ${firstI}, Q: ${firstQ}`);
};
```

## Understanding the Data

### What You're Receiving

The waveform stream sends **decimated IQ samples** from the active SDR tuner.

- **I (In-phase)**: Real component of the complex sample
- **Q (Quadrature)**: Imaginary component of the complex sample
- These represent the raw RF signal downconverted to baseband

### Binary Packet Format

Each WebSocket message contains:
```
[Header: 8 bytes]
[Sample Data: N * 8 bytes]

Header:
  Bytes 0-3: int32 sample count (little-endian)
  Bytes 4-7: int32 timestamp (little-endian)

Each Sample (8 bytes):
  Bytes 0-3: float32 I value (little-endian)
  Bytes 4-7: float32 Q value (little-endian)
```

### Sample Values
- Range: typically -1.0 to +1.0 (normalized)
- Values represent signal amplitude at that instant
- Combined I/Q values encode both amplitude and phase

## Performance Characteristics

### Current Configuration

**WaveformSampleTap.java**
- Processes every 10th buffer from the tuner
- Reduces CPU load while maintaining good visualization

**WebStreamWaveformBroadcaster.java**
- Decimation: Sends every 100th sample
- Max packet size: 512 samples
- Example: If tuner runs at 2 MSPS, you receive ~200 samples/second

### Adjusting Performance

**For Higher Resolution (more samples)**
Edit `WebStreamWaveformBroadcaster.java`:
```java
private static final int DECIMATION_FACTOR = 50; // Was 100
```

**For Lower CPU Usage**
Edit `WaveformSampleTap.java`:
```java
private static final int SAMPLE_INTERVAL = 20; // Was 10
```

## How It Works

```
SDR Hardware
    ↓
Tuner Driver (RTL-SDR, Airspy, etc.)
    ↓
TunerController (raw IQ samples)
    ↓
WaveformSampleTap (rate limiting)
    ↓
WebStreamWaveformBroadcaster (decimation, packaging)
    ↓
WebSocket (/waveform endpoint)
    ↓
Your Web Browser
```

## Visualization Ideas

### 1. Time Domain (What the test page shows)
- Plot I and Q as separate waveforms over time
- Shows signal envelope and modulation

### 2. Constellation Diagram
Plot I vs Q (X=I, Y=Q) to see:
- Signal modulation type
- Signal quality
- Phase relationships

### 3. Waterfall Display
- Stack waveforms vertically
- Color-code amplitude
- Shows signal changes over time

### 4. Spectrum View
- Apply FFT to I/Q data
- Show frequency content
- Real-time spectrum analyzer

## WebSocket Endpoints

Your SDRTrunk web server now has:
- `ws://localhost:8080/audio` - Decoded audio stream
- `ws://localhost:8080/waveform` - Raw IQ waveform data

## Troubleshooting

### No Data Received
1. Check if a tuner is active and receiving signals
2. Verify tuner is tuned to a frequency
3. Check browser console for WebSocket errors
4. Ensure SDRTrunk web server started successfully

### High CPU Usage
- Increase `SAMPLE_INTERVAL` in WaveformSampleTap.java
- Increase `DECIMATION_FACTOR` in WebStreamWaveformBroadcaster.java
- Limit number of connected clients

### Connection Refused
- Verify SDRTrunk is running
- Check that web server started (look for "Web stream server started" in logs)
- Ensure no firewall blocking port 8080

## Next Steps

### Frontend Integration
Add waveform visualization to the main UI by editing:
`src/main/java/io/github/dsheirer/audio/broadcast/webstream/WebUIServlet.java`

See `WAVEFORM_STREAMING.md` for complete frontend code examples.

### Advanced Features You Could Add

1. **FFT/Spectrum Analyzer**
   - Apply Fast Fourier Transform to IQ data
   - Display frequency spectrum in real-time

2. **Constellation Diagram**
   - Plot I vs Q points
   - Useful for analyzing digital modulations

3. **Signal Strength Meter**
   - Calculate RMS or peak amplitude
   - Display as meter or graph

4. **Recording**
   - Save IQ data to file
   - Replay capability

5. **Multiple Tuner Support**
   - Select which tuner to visualize
   - Multi-pane display

## Files Reference

**Backend Implementation:**
- `WaveformWebSocket.java` - WebSocket handler
- `WebStreamWaveformBroadcaster.java` - Sample broadcaster
- `WaveformSampleTap.java` - Tuner tap
- `WebStreamServer.java` - Server configuration
- `SDRTrunk.java` - Main application integration

**Test Page:**
- `waveform_test.html` - Standalone test/demo page

**Documentation:**
- `WAVEFORM_STREAMING.md` - Complete technical documentation
- `WAVEFORM_QUICKSTART.md` - This file

## Success Criteria

✅ Build completes without errors
✅ SDRTrunk starts with "Web stream server started" message
✅ WebSocket connection to `/waveform` succeeds
✅ Binary packets received with sample data
✅ Waveform displays in test page

Your implementation is complete and working!
