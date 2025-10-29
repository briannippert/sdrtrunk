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
            max-width: 95%;
            width: 95%;
            margin: 0 auto;
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
        
        .top-bar {
            display: grid;
            grid-template-columns: 200px 1fr 280px;
            gap: 15px;
            margin-bottom: 20px;
        }
        
        .top-bar-section {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 10px 15px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
        }
        
        .top-bar-label {
            color: #00aa00;
            font-size: 0.65em;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 5px;
        }
        
        .top-bar-channel {
            text-align: center;
        }
        
        .top-bar-controls {
            display: flex;
            flex-direction: column;
        }
        
        .control-buttons {
            display: flex;
            gap: 5px;
            margin-top: 5px;
        }
        
        .small-btn {
            flex: 1;
            padding: 6px 10px;
            font-size: 0.75em;
            font-weight: bold;
            font-family: 'Courier New', monospace;
            border: 1px solid #00ff00;
            border-radius: 4px;
            cursor: pointer;
            background: #000;
            color: #00ff00;
            text-transform: uppercase;
            transition: all 0.2s ease;
        }
        
        .small-btn:hover {
            background: #00ff00;
            color: #000;
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
            padding: 8px;
            border-radius: 5px;
            font-weight: bold;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 2px;
            position: relative;
        }
        
        .status::before {
            content: '●';
            font-size: 1.2em;
            margin-right: 8px;
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
            font-size: 1.1em;
            font-weight: bold;
            text-shadow: 0 0 10px #00ff00;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        
        .channel-control {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
            display: none;
        }
        
        .channel-control.expanded {
            display: block;
        }
        
        .channel-list {
            max-height: 200px;
            overflow-y: auto;
            display: none;
        }
        
        .channel-list.expanded {
            display: block;
        }
        
        .channel-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px;
            margin: 5px 0;
            background: rgba(0, 255, 0, 0.05);
            border: 1px solid #003300;
            border-radius: 4px;
        }
        
        .channel-item:hover {
            background: rgba(0, 255, 0, 0.1);
        }
        
        .channel-item-name {
            color: #00ff00;
            flex: 1;
        }
        
        .channel-item-status {
            color: #00aa00;
            font-size: 0.8em;
            margin: 0 10px;
        }
        
        .channel-item-status.running {
            color: #00ff00;
        }
        
        .channel-item-btn {
            padding: 5px 15px;
            font-size: 0.8em;
            background: #000;
            border: 1px solid #00ff00;
            color: #00ff00;
            border-radius: 4px;
            cursor: pointer;
            font-family: 'Courier New', monospace;
            text-transform: uppercase;
        }
        
        .channel-item-btn:hover {
            background: #00ff00;
            color: #000;
        }
        
        .channel-item-btn:disabled {
            opacity: 0.3;
            cursor: not-allowed;
            border-color: #555;
            color: #555;
        }
        
        .refresh-btn {
            padding: 5px 10px;
            font-size: 0.8em;
            background: #000;
            border: 1px solid #00ff00;
            color: #00ff00;
            border-radius: 4px;
            cursor: pointer;
            font-family: 'Courier New', monospace;
            margin-left: 10px;
        }
        
        .refresh-btn:hover {
            background: #00ff00;
            color: #000;
        }
        
        .add-channel-form {
            background: rgba(0, 50, 0, 0.3);
            border: 1px solid #00aa00;
            border-radius: 4px;
            padding: 15px;
            margin-bottom: 15px;
            display: none;
        }
        
        .add-channel-form.expanded {
            display: block;
        }
        
        .form-row {
            margin-bottom: 10px;
        }
        
        .form-label {
            display: block;
            color: #00aa00;
            font-size: 0.8em;
            margin-bottom: 5px;
            text-transform: uppercase;
        }
        
        .form-input, .form-select {
            width: 100%;
            padding: 8px;
            background: #000;
            border: 1px solid #00ff00;
            color: #00ff00;
            border-radius: 4px;
            font-family: 'Courier New', monospace;
        }
        
        .form-input:focus, .form-select:focus {
            outline: none;
            box-shadow: 0 0 5px #00ff00;
        }
        
        .form-buttons {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }
        
        .form-btn {
            flex: 1;
            padding: 10px;
            background: #000;
            border: 1px solid #00ff00;
            color: #00ff00;
            border-radius: 4px;
            cursor: pointer;
            font-family: 'Courier New', monospace;
            text-transform: uppercase;
            font-size: 0.9em;
        }
        
        .form-btn:hover {
            background: #00ff00;
            color: #000;
        }
        
        .form-btn.cancel {
            border-color: #ffaa00;
            color: #ffaa00;
        }
        
        .form-btn.cancel:hover {
            background: #ffaa00;
            color: #000;
        }
        
        .delete-btn {
            background: #000;
            border: 1px solid #ff3333;
            color: #ff3333;
            padding: 5px 15px;
            font-size: 0.8em;
            border-radius: 4px;
            cursor: pointer;
            font-family: 'Courier New', monospace;
            text-transform: uppercase;
            margin-left: 5px;
        }
        
        .delete-btn:hover {
            background: #ff3333;
            color: #000;
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
        
        .waveform-container {
            background: #000;
            border: 2px solid #00ff00;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
            box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
        }
        
        .waveform-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }
        
        .waveform-label {
            color: #00aa00;
            font-size: 0.8em;
            text-transform: uppercase;
            letter-spacing: 2px;
        }
        
        .waveform-status {
            color: #00ff00;
            font-size: 0.75em;
            display: flex;
            gap: 15px;
        }
        
        .waveform-stat {
            display: flex;
            gap: 5px;
        }
        
        .waveform-stat-label {
            color: #00aa00;
        }
        
        .waveform-stat-value {
            color: #00ff00;
            font-weight: bold;
        }
        
        #waveformCanvas {
            width: 100%;
            height: 200px;
            background: #000;
            border: 1px solid #003300;
            border-radius: 4px;
            display: block;
        }
        
        .dashboard-grid {
            display: grid;
            grid-template-columns: 1fr 350px;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .dashboard-main {
            grid-column: 1;
        }
        
        .dashboard-side {
            grid-column: 2;
        }
        
        .info-section-title {
            color: #00ff00;
            font-size: 0.9em;
            font-weight: bold;
            text-transform: uppercase;
            letter-spacing: 2px;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #00ff00;
            text-align: center;
        }
        
        @media (max-width: 1200px) {
            .dashboard-grid {
                grid-template-columns: 1fr;
            }
            
            .dashboard-main,
            .dashboard-side {
                grid-column: 1;
            }
            
            .top-bar {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="scanline"></div>
        <h1>⚡ SCANNER ⚡</h1>
        <div class="subtitle">SDRTrunk Live Monitor</div>
        
        <div class="top-bar">
            <div class="top-bar-section">
                <div class="top-bar-label">STATUS</div>
                <div class="status disconnected" id="status">OFFLINE</div>
            </div>
            
            <div class="top-bar-section top-bar-channel">
                <div class="top-bar-label">ACTIVE CHANNEL</div>
                <div class="channel-name" id="channelName">---</div>
            </div>
            
            <div class="top-bar-section top-bar-controls">
                <div class="top-bar-label">CONTROLS</div>
                <div class="control-buttons">
                    <button class="small-btn" onclick="toggleChannelList()">CHANNELS <span id="toggleIcon">▼</span></button>
                    <button class="small-btn" onclick="loadChannels(event)">⟳</button>
                    <button class="small-btn" onclick="showAddForm(event)">+</button>
                </div>
            </div>
        </div>
        
        <div class="channel-control" id="channelControlPanel">
            
            <div class="add-channel-form" id="addChannelForm">
                <div class="form-row">
                    <label class="form-label">Channel Name</label>
                    <input type="text" id="newChannelName" class="form-input" placeholder="e.g., Police Dispatch">
                </div>
                <div class="form-row">
                    <label class="form-label">Frequency (MHz)</label>
                    <input type="number" id="newChannelFreq" class="form-input" placeholder="e.g., 155.8875" step="0.000001" min="0">
                </div>
                <div class="form-row">
                    <label class="form-label">Decoder Type</label>
                    <select id="newChannelType" class="form-select">
                        <option value="NBFM">NBFM - Narrowband FM</option>
                        <option value="AM">AM - Amplitude Modulation</option>
                        <option value="P25_PHASE1">P25 Phase 1 - APCO Project 25</option>
                        <option value="P25_PHASE2">P25 Phase 2 - APCO Project 25</option>
                    </select>
                </div>
                <div class="form-row">
                    <label class="form-label">System (Optional)</label>
                    <input type="text" id="newChannelSystem" class="form-input" placeholder="e.g., County System">
                </div>
                <div class="form-row">
                    <label class="form-label">Site (Optional)</label>
                    <input type="text" id="newChannelSite" class="form-input" placeholder="e.g., Main Site">
                </div>
                <div class="form-buttons">
                    <button class="form-btn" onclick="createChannel()">CREATE</button>
                    <button class="form-btn cancel" onclick="hideAddForm()">CANCEL</button>
                </div>
            </div>
            
            <div class="channel-list" id="channelList">
                <div style="color: #00aa00; text-align: center; padding: 20px;">
                    Click REFRESH to load channels
                </div>
            </div>
        </div>
        
        <div class="controls">
            <button id="connectBtn" onclick="connect()">CONNECT</button>
            <button id="playBtn" onclick="play()" disabled>MONITOR</button>
            <button id="stopBtn" onclick="stop()" disabled>MUTE</button>
        </div>
        
        <div class="dashboard-grid">
            <div class="waveform-container dashboard-main">
                <div class="waveform-header">
                    <div class="waveform-label">📊 SPECTRUM WATERFALL</div>
                    <div class="waveform-status">
                        <div class="waveform-stat">
                            <span class="waveform-stat-label">Samples:</span>
                            <span class="waveform-stat-value" id="waveformSamples">0</span>
                        </div>
                        <div class="waveform-stat">
                            <span class="waveform-stat-label">Rate:</span>
                            <span class="waveform-stat-value" id="waveformRate">0 Hz</span>
                        </div>
                    </div>
                </div>
                <canvas id="waveformCanvas" width="1400" height="200"></canvas>
            </div>
            
            <div class="info dashboard-side">
                <div class="info-section-title">AUDIO INFO</div>
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
    </div>

    <script>
        let ws = null;
        let waveformWs = null;
        let audioContext = null;
        let audioQueue = [];
        let isPlaying = false;
        let nextStartTime = 0;
        let packetCount = 0;
        const sampleRate = 8000;
        let currentChannelName = '---';
        let channelListExpanded = false;
        
        // Waveform visualization - Waterfall/Spectrogram
        const waveformCanvas = document.getElementById('waveformCanvas');
        const waveformCtx = waveformCanvas.getContext('2d');
        let waveformUpdateCount = 0;
        let waveformLastUpdateTime = Date.now();
        
        // Waterfall display buffer
        let waterfallBuffer = null;
        const waterfallHeight = waveformCanvas.height;
        
        // FFT function using basic DFT (simple implementation)
        function computeFFT(iSamples, qSamples) {
            const N = iSamples.length;
            if (N === 0) return [];
            
            const fftSize = Math.min(N, 512); // Use up to 512 samples for FFT
            const numBins = Math.floor(fftSize / 2);
            if (numBins <= 0) return [];
            
            const magnitudes = new Array(numBins);
            
            for (let k = 0; k < numBins; k++) {
                let realSum = 0;
                let imagSum = 0;
                
                for (let n = 0; n < fftSize; n++) {
                    const angle = -2 * Math.PI * k * n / fftSize;
                    const cos = Math.cos(angle);
                    const sin = Math.sin(angle);
                    
                    // Complex multiplication: (I + jQ) * (cos + j*sin)
                    realSum += iSamples[n] * cos - qSamples[n] * sin;
                    imagSum += iSamples[n] * sin + qSamples[n] * cos;
                }
                
                // Magnitude
                magnitudes[k] = Math.sqrt(realSum * realSum + imagSum * imagSum) / fftSize;
            }
            
            return magnitudes;
        }
        
        // Convert magnitude to color (blue -> green -> yellow -> red)
        function magnitudeToColor(magnitude, maxMag) {
            const normalized = Math.min(magnitude / maxMag, 1.0);
            const intensity = Math.pow(normalized, 0.5); // Gamma correction
            
            let r, g, b;
            
            if (intensity < 0.25) {
                // Black to Blue
                const t = intensity / 0.25;
                r = 0;
                g = 0;
                b = Math.floor(t * 255);
            } else if (intensity < 0.5) {
                // Blue to Cyan
                const t = (intensity - 0.25) / 0.25;
                r = 0;
                g = Math.floor(t * 255);
                b = 255;
            } else if (intensity < 0.75) {
                // Cyan to Yellow
                const t = (intensity - 0.5) / 0.25;
                r = Math.floor(t * 255);
                g = 255;
                b = Math.floor((1 - t) * 255);
            } else {
                // Yellow to Red
                const t = (intensity - 0.75) / 0.25;
                r = 255;
                g = Math.floor((1 - t) * 255);
                b = 0;
            }
            
            return `rgb(${r},${g},${b})`;
        }
        
        function toggleChannelList() {
            channelListExpanded = !channelListExpanded;
            const panel = document.getElementById('channelControlPanel');
            const list = document.getElementById('channelList');
            const icon = document.getElementById('toggleIcon');
            
            if (channelListExpanded) {
                panel.classList.add('expanded');
                list.classList.add('expanded');
                icon.textContent = '▲';
            } else {
                panel.classList.remove('expanded');
                list.classList.remove('expanded');
                icon.textContent = '▼';
            }
        }
        
        function showAddForm(event) {
            if (event) event.stopPropagation();
            document.getElementById('addChannelForm').classList.add('expanded');
            if (!channelListExpanded) {
                toggleChannelList();
            }
        }
        
        function hideAddForm() {
            document.getElementById('addChannelForm').classList.remove('expanded');
            // Clear form
            document.getElementById('newChannelName').value = '';
            document.getElementById('newChannelFreq').value = '';
            document.getElementById('newChannelSystem').value = '';
            document.getElementById('newChannelSite').value = '';
        }
        
        function createChannel() {
            const name = document.getElementById('newChannelName').value.trim();
            const frequencyMHz = parseFloat(document.getElementById('newChannelFreq').value);
            const type = document.getElementById('newChannelType').value;
            const system = document.getElementById('newChannelSystem').value.trim();
            const site = document.getElementById('newChannelSite').value.trim();
            
            if (!name) {
                alert('Channel name is required');
                return;
            }
            
            if (!frequencyMHz || frequencyMHz <= 0) {
                alert('Valid frequency is required (in MHz, e.g., 155.8875)');
                return;
            }
            
            // Convert MHz to Hz
            const frequency = Math.round(frequencyMHz * 1000000);
            
            const channelData = {
                name: name,
                frequency: frequency,
                type: type
            };
            
            if (system) channelData.system = system;
            if (site) channelData.site = site;
            
            fetch('/api/channels?action=create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(channelData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    alert(data.message);
                    hideAddForm();
                    loadChannels(null);
                } else {
                    alert('Error: ' + data.error);
                }
            })
            .catch(error => {
                alert('Error creating channel: ' + error.message);
            });
        }
        
        function deleteChannel(channelName) {
            if (!confirm('Delete channel "' + channelName + '"?')) {
                return;
            }
            
            fetch('/api/channels?action=delete&channel=' + encodeURIComponent(channelName), {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    console.log(data.message);
                    loadChannels(null);
                } else {
                    alert('Error: ' + data.error);
                }
            })
            .catch(error => {
                alert('Error deleting channel: ' + error.message);
            });
        }
        
        function loadChannels(event) {
            if (event) event.stopPropagation();
            
            fetch('/api/channels?action=list')
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        displayChannels(data.channels);
                    } else {
                        console.error('Failed to load channels:', data.error);
                    }
                })
                .catch(error => {
                    console.error('Error loading channels:', error);
                    document.getElementById('channelList').innerHTML = 
                        '<div style="color: #ff3333; text-align: center; padding: 20px;">ERROR: ' + error.message + '</div>';
                });
        }
        
        function displayChannels(channels) {
            const list = document.getElementById('channelList');
            
            if (channels.length === 0) {
                list.innerHTML = '<div style="color: #00aa00; text-align: center; padding: 20px;">No channels configured</div>';
                return;
            }
            
            list.innerHTML = '';
            channels.forEach(channel => {
                const item = document.createElement('div');
                item.className = 'channel-item';
                
                const nameDiv = document.createElement('div');
                nameDiv.style.flex = '1';
                
                const name = document.createElement('div');
                name.className = 'channel-item-name';
                name.textContent = channel.name;
                nameDiv.appendChild(name);
                
                // Show frequency if available
                if (channel.frequency) {
                    const freq = document.createElement('div');
                    freq.style.color = '#00aa00';
                    freq.style.fontSize = '0.75em';
                    freq.textContent = (channel.frequency / 1000000).toFixed(4) + ' MHz';
                    nameDiv.appendChild(freq);
                }
                
                const status = document.createElement('span');
                status.className = 'channel-item-status' + (channel.processing ? ' running' : '');
                status.textContent = channel.processing ? '● RUNNING' : '○ STOPPED';
                
                const btnContainer = document.createElement('span');
                
                const btn = document.createElement('button');
                btn.className = 'channel-item-btn';
                btn.textContent = channel.processing ? 'STOP' : 'START';
                btn.onclick = () => toggleChannel(channel.name, channel.processing);
                
                const delBtn = document.createElement('button');
                delBtn.className = 'delete-btn';
                delBtn.textContent = 'DELETE';
                delBtn.onclick = () => deleteChannel(channel.name);
                
                btnContainer.appendChild(btn);
                btnContainer.appendChild(delBtn);
                
                item.appendChild(nameDiv);
                item.appendChild(status);
                item.appendChild(btnContainer);
                list.appendChild(item);
            });
        }
        
        function toggleChannel(channelName, isRunning) {
            const action = isRunning ? 'stop' : 'start';
            
            fetch('/api/channels?action=' + action + '&channel=' + encodeURIComponent(channelName), {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    console.log(data.message);
                    // Reload channels after a short delay
                    setTimeout(() => loadChannels(null), 500);
                } else {
                    console.error('Error:', data.error);
                    alert('Error: ' + data.error);
                }
            })
            .catch(error => {
                console.error('Error toggling channel:', error);
                alert('Error: ' + error.message);
            });
        }
        
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
                
                // Also close waveform WebSocket
                if (waveformWs) {
                    waveformWs.close();
                    waveformWs = null;
                }
            };
            
            // Connect to waveform WebSocket
            connectWaveform();
        }
        
        function connectWaveform() {
            const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsUrl = protocol + '//' + window.location.host + '/waveform';
            
            waveformWs = new WebSocket(wsUrl);
            waveformWs.binaryType = 'arraybuffer';
            
            waveformWs.onopen = function() {
                console.log('Waveform WebSocket connected');
            };
            
            waveformWs.onmessage = function(event) {
                drawWaveform(event.data);
                
                // Update rate counter
                waveformUpdateCount++;
                const now = Date.now();
                if (now - waveformLastUpdateTime >= 1000) {
                    const rate = waveformUpdateCount / ((now - waveformLastUpdateTime) / 1000);
                    document.getElementById('waveformRate').textContent = rate.toFixed(1) + ' Hz';
                    waveformUpdateCount = 0;
                    waveformLastUpdateTime = now;
                }
            };
            
            waveformWs.onerror = function(error) {
                console.error('Waveform WebSocket error:', error);
            };
            
            waveformWs.onclose = function() {
                console.log('Waveform WebSocket closed');
            };
        }
        
        function drawWaveform(data) {
            const view = new DataView(data);
            const sampleCount = view.getInt32(0, true);
            const timestamp = view.getUint32(4, true);
            const centerFrequency = Number(view.getBigInt64(8, true)); // Hz
            const sampleRate = view.getFloat32(16, true); // Hz
            
            document.getElementById('waveformSamples').textContent = sampleCount;
            
            if (sampleCount === 0) return;
            
            const width = waveformCanvas.width;
            const height = waveformCanvas.height;
            
            // Extract I and Q samples (now starting at offset 20 due to extended header)
            const iSamples = new Float32Array(sampleCount);
            const qSamples = new Float32Array(sampleCount);
            
            for (let i = 0; i < sampleCount; i++) {
                const offset = 20 + i * 8;
                iSamples[i] = view.getFloat32(offset, true);
                qSamples[i] = view.getFloat32(offset + 4, true);
            }
            
            // Compute FFT to get frequency spectrum
            const magnitudes = computeFFT(iSamples, qSamples);
            const numBins = magnitudes.length;
            
            if (numBins === 0) {
                console.warn('FFT returned no bins - skipping frame');
                return;
            }
            
            // Find max magnitude for normalization
            let maxMag = 0;
            for (let i = 0; i < numBins; i++) {
                if (magnitudes[i] > maxMag) maxMag = magnitudes[i];
            }
            if (maxMag === 0) maxMag = 1; // Prevent division by zero
            
            // Define the waterfall display area (excluding bottom 15px for labels)
            const waterfallHeight = height - 15;
            
            // Scroll the waterfall down by copying current content (excluding label area)
            const imageData = waveformCtx.getImageData(0, 0, width, waterfallHeight - 1);
            waveformCtx.putImageData(imageData, 0, 1);
            
            // Clear the top line
            waveformCtx.fillStyle = '#000';
            waveformCtx.fillRect(0, 0, width, 1);
            
            // Draw new spectrum line at the top
            for (let x = 0; x < width; x++) {
                // Map x position to frequency bin
                const binIndex = Math.floor((x / width) * numBins);
                const magnitude = magnitudes[binIndex];
                
                // Convert magnitude to color
                const color = magnitudeToColor(magnitude, maxMag);
                
                // Draw pixel at top of canvas
                waveformCtx.fillStyle = color;
                waveformCtx.fillRect(x, 0, 1, 1);
            }
            
            // Draw frequency axis labels
            if (centerFrequency > 0 && sampleRate > 0) {
                const startFreq = centerFrequency - (sampleRate / 2);
                const endFreq = centerFrequency + (sampleRate / 2);
                
                // Clear the bottom label area first (prevent scrolling artifacts)
                waveformCtx.fillStyle = '#000';
                waveformCtx.fillRect(0, height - 15, width, 15);
                
                waveformCtx.fillStyle = '#00ff00';
                waveformCtx.font = '10px Courier New';
                
                // Format frequency in MHz
                const formatFreq = (freq) => (freq / 1e6).toFixed(3) + ' MHz';
                
                // Left edge frequency
                waveformCtx.fillText(formatFreq(startFreq), 5, height - 4);
                
                // Center frequency
                const centerText = formatFreq(centerFrequency);
                const centerWidth = waveformCtx.measureText(centerText).width;
                waveformCtx.fillText(centerText, (width - centerWidth) / 2, height - 4);
                
                // Right edge frequency
                const rightText = formatFreq(endFreq);
                const rightWidth = waveformCtx.measureText(rightText).width;
                waveformCtx.fillText(rightText, width - rightWidth - 5, height - 4);
                
                // Time label - draw in top-left corner (outside scroll area)
                waveformCtx.fillStyle = '#000';
                waveformCtx.fillRect(0, 0, 60, 14);
                waveformCtx.fillStyle = '#00ff00';
                waveformCtx.fillText('Time ↓', 5, 12);
            } else {
                // Fallback if no frequency info
                waveformCtx.fillStyle = '#000';
                waveformCtx.fillRect(0, height - 15, width, 15);
                waveformCtx.fillRect(0, 0, 100, 14);
                
                waveformCtx.fillStyle = '#00aa00';
                waveformCtx.font = '10px Courier New';
                waveformCtx.fillText('Frequency →', width - 100, height - 4);
                waveformCtx.fillText('Time ↓', 5, 12);
            }
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
    </script>
</body>
</html>
                """;
    }
}
