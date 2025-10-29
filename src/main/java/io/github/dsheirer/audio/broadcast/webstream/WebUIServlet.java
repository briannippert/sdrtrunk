/*
 * *****************************************************************************
 * Copyright (C) 2014-2025 Dennis Sheirer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * ****************************************************************************
 */

package io.github.dsheirer.audio.broadcast.webstream;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

public class WebUIServlet extends HttpServlet
{
    private static final Logger mLog = LoggerFactory.getLogger(WebUIServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = resp.getWriter();
        out.println(getHTML());
    }

    private String getHTML()
    {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SDRTrunk Audio Stream</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Courier New', monospace;
            background: #0a0a0a;
            color: #00ff00;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 20px;
        }
        
        .container {
            background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
            border: 3px solid #333;
            border-radius: 15px;
            box-shadow: 0 0 40px rgba(0, 255, 0, 0.3), inset 0 0 20px rgba(0, 0, 0, 0.5);
            padding: 30px;
            max-width: 600px;
            width: 100%;
            position: relative;
        }
        
        .container::before {
            content: '';
            position: absolute;
            top: -3px;
            left: -3px;
            right: -3px;
            bottom: -3px;
            background: linear-gradient(45deg, #00ff00, #00aa00, #00ff00);
            border-radius: 15px;
            z-index: -1;
            opacity: 0.3;
        }
        
        h1 {
            color: #00ff00;
            text-align: center;
            margin-bottom: 5px;
            font-size: 2.2em;
            text-transform: uppercase;
            letter-spacing: 3px;
            text-shadow: 0 0 10px #00ff00, 0 0 20px #00ff00;
        }
        
        .subtitle {
            text-align: center;
            color: #00aa00;
            margin-bottom: 25px;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 2px;
        }
        
        .status-panel {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
        }
        
        .status {
            text-align: center;
            padding: 12px;
            border-radius: 5px;
            font-weight: bold;
            font-size: 1.2em;
            text-transform: uppercase;
            letter-spacing: 2px;
            position: relative;
        }
        
        .status::before {
            content: '●';
            font-size: 1.5em;
            margin-right: 10px;
            animation: pulse 1.5s infinite;
        }
        
        .status.disconnected {
            background: rgba(255, 0, 0, 0.2);
            color: #ff3333;
            border: 1px solid #ff3333;
        }
        
        .status.disconnected::before {
            color: #ff3333;
        }
        
        .status.connected {
            background: rgba(255, 255, 0, 0.2);
            color: #ffff00;
            border: 1px solid #ffff00;
        }
        
        .status.connected::before {
            color: #ffff00;
        }
        
        .status.playing {
            background: rgba(0, 255, 0, 0.2);
            color: #00ff00;
            border: 1px solid #00ff00;
        }
        
        .status.playing::before {
            color: #00ff00;
        }
        
        .controls {
            display: flex;
            gap: 10px;
            margin-bottom: 25px;
        }
        
        button {
            flex: 1;
            padding: 15px;
            font-size: 1em;
            font-weight: bold;
            font-family: 'Courier New', monospace;
            border: 2px solid #00ff00;
            border-radius: 8px;
            cursor: pointer;
            background: #000;
            color: #00ff00;
            text-transform: uppercase;
            letter-spacing: 1px;
            transition: all 0.3s ease;
            box-shadow: 0 0 10px rgba(0, 255, 0, 0.3);
        }
        
        button:hover:not(:disabled) {
            background: #00ff00;
            color: #000;
            box-shadow: 0 0 20px rgba(0, 255, 0, 0.6);
        }
        
        button:active:not(:disabled) {
            transform: scale(0.95);
        }
        
        button:disabled {
            opacity: 0.3;
            cursor: not-allowed;
            border-color: #555;
            color: #555;
            box-shadow: none;
        }
        
        .channel-display {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            text-align: center;
            box-shadow: inset 0 0 15px rgba(0, 255, 0, 0.2);
        }
        
        .channel-label {
            color: #00aa00;
            font-size: 0.8em;
            text-transform: uppercase;
            letter-spacing: 2px;
            margin-bottom: 8px;
        }
        
        .channel-name {
            color: #00ff00;
            font-size: 1.8em;
            font-weight: bold;
            text-shadow: 0 0 10px #00ff00;
            min-height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .visualizer {
            height: 80px;
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            margin-bottom: 20px;
            display: flex;
            align-items: flex-end;
            justify-content: space-around;
            padding: 5px;
            gap: 1px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
        }
        
        .bar {
            flex: 1;
            background: linear-gradient(to top, #00ff00, #00aa00);
            border-radius: 2px;
            transition: height 0.1s ease;
            min-height: 2px;
            box-shadow: 0 0 5px #00ff00;
        }
        
        .info {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 15px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
        }
        
        .info-item {
            display: flex;
            justify-content: space-between;
            padding: 8px 0;
            border-bottom: 1px solid #003300;
            font-size: 0.9em;
        }
        
        .info-item:last-child {
            border-bottom: none;
        }
        
        .info-label {
            color: #00aa00;
            text-transform: uppercase;
            font-size: 0.85em;
            letter-spacing: 1px;
        }
        
        .info-value {
            color: #00ff00;
            font-weight: bold;
        }
        
        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.3; }
        }
        
        @keyframes scanline {
            0% { transform: translateY(-100%); }
            100% { transform: translateY(100%); }
        }
        
        .scanline {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: linear-gradient(to bottom, transparent, #00ff00, transparent);
            opacity: 0.1;
            animation: scanline 4s linear infinite;
            pointer-events: none;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="scanline"></div>
        <h1>⚡ SCANNER ⚡</h1>
        <div class="subtitle">SDRTrunk Live Monitor</div>
        
        <div class="status-panel">
            <div class="status disconnected" id="status">OFFLINE</div>
        </div>
        
        <div class="channel-display">
            <div class="channel-label">[ ACTIVE CHANNEL ]</div>
            <div class="channel-name" id="channelName">---</div>
        </div>
        
        <div class="controls">
            <button id="connectBtn" onclick="connect()">CONNECT</button>
            <button id="playBtn" onclick="play()" disabled>MONITOR</button>
            <button id="stopBtn" onclick="stop()" disabled>MUTE</button>
        </div>
        
        <div class="visualizer" id="visualizer">
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
            <div class="bar"></div>
        </div>
        
        <div class="info">
            <div class="info-item">
                <span class="info-label">Sample Rate</span>
                <span class="info-value">8000 Hz</span>
            </div>
            <div class="info-item">
                <span class="info-label">Audio Mode</span>
                <span class="info-value">MONO</span>
            </div>
            <div class="info-item">
                <span class="info-label">Format</span>
                <span class="info-value">16-BIT PCM</span>
            </div>
            <div class="info-item">
                <span class="info-label">Packets RX</span>
                <span class="info-value" id="packetCount">0</span>
            </div>
        </div>
    </div>

    <script>
        let ws = null;
        let audioContext = null;
        let audioQueue = [];
        let isPlaying = false;
        let nextStartTime = 0;
        let packetCount = 0;
        const sampleRate = 8000;
        let currentChannelName = '---';
        
        function updateStatus(text, className) {
            const status = document.getElementById('status');
            status.textContent = text;
            status.className = 'status ' + className;
        }
        
        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = protocol + '//' + window.location.host + '/audio';
            
            ws = new WebSocket(wsUrl);
            ws.binaryType = 'arraybuffer';
            
            ws.onopen = function() {
                updateStatus('CONNECTED', 'connected');
                document.getElementById('connectBtn').disabled = true;
                document.getElementById('playBtn').disabled = false;
            };
            
            ws.onmessage = function(event) {
                if (event.data instanceof ArrayBuffer) {
                    processAudioData(event.data);
                    packetCount++;
                    document.getElementById('packetCount').textContent = packetCount;
                } else if (typeof event.data === 'string') {
                    processMetadata(event.data);
                }
            };
            
            ws.onerror = function(error) {
                console.error('WebSocket error:', error);
                updateStatus('ERROR', 'disconnected');
            };
            
            ws.onclose = function() {
                updateStatus('OFFLINE', 'disconnected');
                document.getElementById('connectBtn').disabled = false;
                document.getElementById('playBtn').disabled = true;
                document.getElementById('stopBtn').disabled = true;
                document.getElementById('channelName').textContent = '---';
                isPlaying = false;
            };
        }
        
        function play() {
            if (!audioContext) {
                audioContext = new (window.AudioContext || window.webkitAudioContext)({
                    sampleRate: sampleRate
                });
                nextStartTime = audioContext.currentTime;
            }
            
            isPlaying = true;
            updateStatus('MONITORING', 'playing');
            document.getElementById('playBtn').disabled = true;
            document.getElementById('stopBtn').disabled = false;
            
            processQueue();
        }
        
        function stop() {
            isPlaying = false;
            audioQueue = [];
            updateStatus('CONNECTED', 'connected');
            document.getElementById('playBtn').disabled = false;
            document.getElementById('stopBtn').disabled = true;
        }
        
        function processMetadata(jsonString) {
            try {
                const metadata = JSON.parse(jsonString);
                if (metadata.channelName) {
                    currentChannelName = metadata.channelName;
                    document.getElementById('channelName').textContent = metadata.channelName;
                }
            } catch (e) {
                console.error('Error parsing metadata:', e);
            }
        }
        
        function processAudioData(arrayBuffer) {
            const int16Array = new Int16Array(arrayBuffer);
            const float32Array = new Float32Array(int16Array.length);
            
            for (let i = 0; i < int16Array.length; i++) {
                float32Array[i] = int16Array[i] / 32768.0;
            }
            
            audioQueue.push(float32Array);
            updateVisualizer(float32Array);
            
            if (isPlaying) {
                processQueue();
            }
        }
        
        function processQueue() {
            while (audioQueue.length > 0 && isPlaying) {
                const audioData = audioQueue.shift();
                const audioBuffer = audioContext.createBuffer(1, audioData.length, sampleRate);
                audioBuffer.getChannelData(0).set(audioData);
                
                const source = audioContext.createBufferSource();
                source.buffer = audioBuffer;
                source.connect(audioContext.destination);
                
                const currentTime = audioContext.currentTime;
                if (nextStartTime < currentTime) {
                    nextStartTime = currentTime;
                }
                
                source.start(nextStartTime);
                nextStartTime += audioBuffer.duration;
            }
        }
        
        function updateVisualizer(audioData) {
            const bars = document.querySelectorAll('.bar');
            const segmentSize = Math.floor(audioData.length / bars.length);
            
            bars.forEach((bar, index) => {
                const start = index * segmentSize;
                const end = start + segmentSize;
                let sum = 0;
                
                for (let i = start; i < end && i < audioData.length; i++) {
                    sum += Math.abs(audioData[i]);
                }
                
                const average = sum / segmentSize;
                const height = Math.min(100, average * 300);
                bar.style.height = height + '%';
            });
        }
    </script>
</body>
</html>
                """;
    }
}
