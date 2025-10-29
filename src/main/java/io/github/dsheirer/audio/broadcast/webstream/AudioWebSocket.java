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

public class AudioWebSocket extends WebSocketAdapter
{
    private static final Logger mLog = LoggerFactory.getLogger(AudioWebSocket.class);
    private WebStreamAudioBroadcaster mBroadcaster;

    public AudioWebSocket(WebStreamAudioBroadcaster broadcaster)
    {
        mBroadcaster = broadcaster;
    }

    @Override
    public void onWebSocketConnect(Session session)
    {
        super.onWebSocketConnect(session);
        mBroadcaster.addClient(this);
        mLog.debug("WebSocket connected from: " + session.getRemoteAddress());
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason)
    {
        super.onWebSocketClose(statusCode, reason);
        mBroadcaster.removeClient(this);
        mLog.debug("WebSocket closed: " + statusCode + " - " + reason);
    }

    @Override
    public void onWebSocketError(Throwable cause)
    {
        super.onWebSocketError(cause);
        mLog.error("WebSocket error", cause);
    }

    @Override
    public void onWebSocketText(String message)
    {
        mLog.debug("Received text message: " + message);
    }

    public void sendAudioData(byte[] audioData)
    {
        Session session = getSession();
        if(session != null && session.isOpen())
        {
            try
            {
                session.getRemote().sendBytes(ByteBuffer.wrap(audioData));
            }
            catch(Exception e)
            {
                mLog.error("Error sending audio data", e);
            }
        }
    }

    public void sendMetadata(String metadata)
    {
        Session session = getSession();
        if(session != null && session.isOpen())
        {
            try
            {
                session.getRemote().sendString(metadata);
            }
            catch(Exception e)
            {
                mLog.error("Error sending metadata", e);
            }
        }
    }
}
