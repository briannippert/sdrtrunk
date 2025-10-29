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

import io.github.dsheirer.sample.Listener;
import io.github.dsheirer.sample.complex.ComplexSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class WebStreamWaveformBroadcaster implements Listener<ComplexSamples>
{
    private static final Logger mLog = LoggerFactory.getLogger(WebStreamWaveformBroadcaster.class);
    private static final int DECIMATION_FACTOR = 100; // Send every Nth sample to reduce bandwidth
    private static final int MAX_SAMPLES_PER_PACKET = 512; // Maximum samples per WebSocket packet
    
    private List<WaveformWebSocket> mClients = new ArrayList<>();
    private int mSampleCounter = 0;

    public synchronized void addClient(WaveformWebSocket client)
    {
        if(!mClients.contains(client))
        {
            mClients.add(client);
            mLog.info("Waveform client added. Total clients: " + mClients.size());
        }
    }

    public synchronized void removeClient(WaveformWebSocket client)
    {
        mClients.remove(client);
        mLog.info("Waveform client removed. Total clients: " + mClients.size());
    }

    public synchronized int getClientCount()
    {
        return mClients.size();
    }

    @Override
    public void receive(ComplexSamples samples)
    {
        if(samples == null || getClientCount() == 0)
        {
            return;
        }

        try
        {
            float[] iSamples = samples.i();
            float[] qSamples = samples.q();
            
            int totalSamples = Math.min(iSamples.length, qSamples.length);
            int decimatedSamples = Math.min(totalSamples / DECIMATION_FACTOR, MAX_SAMPLES_PER_PACKET);
            
            if(decimatedSamples == 0)
            {
                return;
            }

            // Create byte buffer: 4 bytes per float * 2 (I and Q) * number of samples
            // Plus 8 bytes for header (4 bytes sample count + 4 bytes sample rate)
            ByteBuffer buffer = ByteBuffer.allocate(8 + decimatedSamples * 8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            
            // Header: number of samples (int32) and timestamp (int32, lower 32 bits)
            buffer.putInt(decimatedSamples);
            buffer.putInt((int)(samples.timestamp() & 0xFFFFFFFF));
            
            // Write decimated I/Q samples as 32-bit floats
            for(int i = 0; i < decimatedSamples; i++)
            {
                int index = i * DECIMATION_FACTOR;
                if(index < totalSamples)
                {
                    buffer.putFloat(iSamples[index]);
                    buffer.putFloat(qSamples[index]);
                }
            }
            
            byte[] data = buffer.array();
            broadcastToClients(data);
        }
        catch(Exception e)
        {
            mLog.error("Error processing waveform samples", e);
        }
    }

    private synchronized void broadcastToClients(byte[] data)
    {
        List<WaveformWebSocket> clientsToRemove = new ArrayList<>();
        
        for(WaveformWebSocket client : mClients)
        {
            try
            {
                client.sendWaveformData(data);
            }
            catch(Exception e)
            {
                mLog.error("Error broadcasting to waveform client, marking for removal", e);
                clientsToRemove.add(client);
            }
        }
        
        for(WaveformWebSocket client : clientsToRemove)
        {
            removeClient(client);
        }
    }
}
