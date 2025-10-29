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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class WaveformWebSocket extends WebSocketAdapter
{
    private static final Logger mLog = LoggerFactory.getLogger(WaveformWebSocket.class);
    private WebStreamWaveformBroadcaster mBroadcaster;

    public WaveformWebSocket(WebStreamWaveformBroadcaster broadcaster)
    {
        mBroadcaster = broadcaster;
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        mBroadcaster.addClient(this);
        mLog.info("Waveform WebSocket connected from: " + session.getRemoteAddress());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        mBroadcaster.removeClient(this);
        mLog.info("Waveform WebSocket closed: " + statusCode + " - " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        mLog.error("Waveform WebSocket error", cause);
    }

    @Override
    public void onWebSocketText(String message)
    {
        mLog.debug("Received waveform text message: " + message);
    }

    public void sendWaveformData(byte[] waveformData)
    {
        Session session = getSession();
        if(session != null && session.isOpen())
        {
            try
            {
                session.getRemote().sendBytes(ByteBuffer.wrap(waveformData));
            }
            catch(Exception e)
            {
                mLog.error("Error sending waveform data", e);
            }
        }
    }
}
