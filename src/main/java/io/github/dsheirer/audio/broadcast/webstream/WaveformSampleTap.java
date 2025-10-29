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

import io.github.dsheirer.buffer.INativeBuffer;
import io.github.dsheirer.sample.Listener;
import io.github.dsheirer.sample.complex.ComplexSamples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaveformSampleTap implements Listener<INativeBuffer>
{
    private static final Logger mLog = LoggerFactory.getLogger(WaveformSampleTap.class);
    private WebStreamWaveformBroadcaster mBroadcaster;
    private int mSampleCounter = 0;
    private static final int SAMPLE_INTERVAL = 10; // Process every 10th buffer to reduce load

    public WaveformSampleTap(WebStreamWaveformBroadcaster broadcaster)
    {
        mBroadcaster = broadcaster;
    }

    @Override
    public void receive(INativeBuffer buffer)
    {
        if(buffer == null || mBroadcaster.getClientCount() == 0)
        {
            return;
        }

        // Sample rate limiting - only process every Nth buffer
        mSampleCounter++;
        if(mSampleCounter < SAMPLE_INTERVAL)
        {
            return;
        }
        mSampleCounter = 0;

        try
        {
            // Get the first ComplexSamples from the iterator
            var iterator = buffer.iterator();
            if(iterator.hasNext())
            {
                ComplexSamples samples = iterator.next();
                if(samples != null)
                {
                    mBroadcaster.receive(samples);
                }
            }
        }
        catch(Exception e)
        {
            mLog.error("Error processing waveform sample tap", e);
        }
    }
}
