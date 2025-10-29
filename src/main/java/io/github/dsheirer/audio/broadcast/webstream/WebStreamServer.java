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

import io.github.dsheirer.audio.AudioSegment;
import io.github.dsheirer.sample.Listener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class WebStreamServer implements Listener<AudioSegment>
{
    private static final Logger mLog = LoggerFactory.getLogger(WebStreamServer.class);
    private Server mServer;
    private int mPort;
    private WebStreamAudioBroadcaster mBroadcaster;
    private boolean mRunning = false;

    public WebStreamServer(int port)
    {
        mPort = port;
        mBroadcaster = new WebStreamAudioBroadcaster();
    }

    public void start() throws Exception
    {
        if(mRunning)
        {
            mLog.warn("Web stream server is already running");
            return;
        }

        mServer = new Server();
        ServerConnector connector = new ServerConnector(mServer);
        connector.setHost("0.0.0.0");  // Listen on all network interfaces
        connector.setPort(mPort);
        mServer.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        mServer.setHandler(context);

        ServletHolder holderWebUI = new ServletHolder("web-ui", new WebUIServlet());
        context.addServlet(holderWebUI, "/*");

        JettyWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) ->
        {
            wsContainer.setMaxTextMessageSize(65535);
            wsContainer.setMaxBinaryMessageSize(65535);
            wsContainer.setIdleTimeout(Duration.ofMinutes(10));
            
            wsContainer.addMapping("/audio", (req, resp) -> new AudioWebSocket(mBroadcaster));
        });

        mServer.start();
        mRunning = true;
        mLog.info("Web stream server started on port " + mPort + " (listening on all interfaces)");
        mLog.info("Access the UI at: http://localhost:" + mPort);
    }

    public void stop() throws Exception
    {
        if(mServer != null && mRunning)
        {
            mServer.stop();
            mRunning = false;
            mLog.info("Web stream server stopped");
        }
    }

    public boolean isRunning()
    {
        return mRunning;
    }

    public int getPort()
    {
        return mPort;
    }

    @Override
    public void receive(AudioSegment audioSegment)
    {
        if(mRunning && audioSegment != null)
        {
            // Listen for when the audio segment is complete
            audioSegment.completeProperty().addListener(new AudioSegmentCompletionMonitor(audioSegment));
        }
    }

    /**
     * Monitors an audio segment for completion and broadcasts it when ready
     */
    public class AudioSegmentCompletionMonitor implements ChangeListener<Boolean>
    {
        private AudioSegment mAudioSegment;

        public AudioSegmentCompletionMonitor(AudioSegment audioSegment)
        {
            mAudioSegment = audioSegment;
            // Increment consumer count since we're going to process this segment
            mAudioSegment.incrementConsumerCount();
        }

        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
        {
            if(newValue)
            {
                try
                {
                    if(mAudioSegment.hasAudio())
                    {
                        mLog.info("WebStream broadcasting completed segment - Channel: '{}', Buffers: {}, Clients: {}", 
                                 mAudioSegment.getChannelName(), mAudioSegment.getAudioBufferCount(), 
                                 mBroadcaster.getClientCount());
                        mBroadcaster.receive(mAudioSegment);
                    }
                    else
                    {
                        mLog.debug("Completed audio segment has no audio - Channel: '{}'", mAudioSegment.getChannelName());
                    }
                }
                finally
                {
                    // Decrement consumer count now that we're done processing
                    mAudioSegment.decrementConsumerCount();
                }
            }
        }
    }
}
