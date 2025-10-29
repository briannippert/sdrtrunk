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

import com.google.gson.Gson;
import io.github.dsheirer.audio.AudioSegment;
import io.github.dsheirer.sample.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebStreamAudioBroadcaster implements Listener<AudioSegment>
{
    private static final Logger mLog = LoggerFactory.getLogger(WebStreamAudioBroadcaster.class);
    private List<AudioWebSocket> mClients = new CopyOnWriteArrayList<>();
    private Gson mGson = new Gson();

    public void addClient(AudioWebSocket client)
    {
        mClients.add(client);
        mLog.info("WebSocket client connected. Total clients: " + mClients.size());
    }

    public void removeClient(AudioWebSocket client)
    {
        mClients.remove(client);
        mLog.info("WebSocket client disconnected. Total clients: " + mClients.size());
    }

    @Override
    public void receive(AudioSegment audioSegment)
    {
        if(audioSegment != null && audioSegment.hasAudio() && !mClients.isEmpty())
        {
            byte[] audioData = convertToBytes(audioSegment);
            String metadata = createMetadata(audioSegment);
            
            for(AudioWebSocket client : mClients)
            {
                try
                {
                    client.sendMetadata(metadata);
                    client.sendAudioData(audioData);
                }
                catch(Exception e)
                {
                    mLog.error("Error sending audio to WebSocket client", e);
                }
            }
        }
    }

    private String createMetadata(AudioSegment audioSegment)
    {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("channelName", audioSegment.getChannelName() != null ? audioSegment.getChannelName() : "Unknown");
        metadata.put("timestamp", audioSegment.getStartTimestamp());
        metadata.put("duration", audioSegment.getDuration());
        metadata.put("encrypted", audioSegment.isEncrypted());
        metadata.put("timeslot", audioSegment.getTimeslot());
        
        return mGson.toJson(metadata);
    }

    private byte[] convertToBytes(AudioSegment audioSegment)
    {
        List<float[]> audioBuffers = audioSegment.getAudioBuffers();
        int totalSamples = 0;
        
        for(float[] buffer : audioBuffers)
        {
            totalSamples += buffer.length;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(totalSamples * 2);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        for(float[] buffer : audioBuffers)
        {
            for(float sample : buffer)
            {
                short shortSample = (short)(sample * 32767.0f);
                byteBuffer.putShort(shortSample);
            }
        }

        return byteBuffer.array();
    }

    public int getClientCount()
    {
        return mClients.size();
    }
}
