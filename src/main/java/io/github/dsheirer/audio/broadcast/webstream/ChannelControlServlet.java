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
import io.github.dsheirer.controller.channel.Channel;
import io.github.dsheirer.controller.channel.ChannelModel;
import io.github.dsheirer.controller.channel.ChannelProcessingManager;
import io.github.dsheirer.module.decode.DecoderType;
import io.github.dsheirer.module.decode.am.DecodeConfigAM;
import io.github.dsheirer.module.decode.config.DecodeConfiguration;
import io.github.dsheirer.module.decode.nbfm.DecodeConfigNBFM;
import io.github.dsheirer.source.config.SourceConfigTuner;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelControlServlet extends HttpServlet
{
    private static final Logger mLog = LoggerFactory.getLogger(ChannelControlServlet.class);
    private ChannelModel mChannelModel;
    private ChannelProcessingManager mChannelProcessingManager;
    private Gson mGson = new Gson();

    public ChannelControlServlet(ChannelModel channelModel, ChannelProcessingManager channelProcessingManager)
    {
        mChannelModel = channelModel;
        mChannelProcessingManager = channelProcessingManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);

        String action = req.getParameter("action");
        
        if("list".equals(action))
        {
            listChannels(resp);
        }
        else if("types".equals(action))
        {
            listDecoderTypes(resp);
        }
        else
        {
            sendError(resp, "Unknown action");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setContentType("application/json");
        
        String action = req.getParameter("action");
        String channelName = req.getParameter("channel");
        
        if("start".equals(action) && channelName != null)
        {
            startChannel(channelName, resp);
        }
        else if("stop".equals(action) && channelName != null)
        {
            stopChannel(channelName, resp);
        }
        else if("delete".equals(action) && channelName != null)
        {
            deleteChannel(channelName, resp);
        }
        else if("create".equals(action))
        {
            createChannel(req, resp);
        }
        else
        {
            sendError(resp, "Invalid action or missing parameters");
        }
    }

    private void listChannels(HttpServletResponse resp) throws IOException
    {
        List<Map<String, Object>> channelList = new ArrayList<>();
        
        for(Channel channel : mChannelModel.getChannels())
        {
            channelList.add(createChannelInfo(channel));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("channels", channelList);
        
        PrintWriter out = resp.getWriter();
        out.println(mGson.toJson(response));
    }

    private void startChannel(String channelName, HttpServletResponse resp) throws IOException
    {
        Channel channel = findChannel(channelName);
        
        if(channel == null)
        {
            sendError(resp, "Channel not found: " + channelName);
            return;
        }
        
        try
        {
            if(channel.isProcessing())
            {
                sendError(resp, "Channel is already running");
                return;
            }
            
            mChannelProcessingManager.start(channel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Channel started: " + channelName);
            
            PrintWriter out = resp.getWriter();
            out.println(mGson.toJson(response));
            
            mLog.info("WebUI started channel: " + channelName);
        }
        catch(Exception e)
        {
            mLog.error("Error starting channel: " + channelName, e);
            sendError(resp, "Error starting channel: " + e.getMessage());
        }
    }

    private void stopChannel(String channelName, HttpServletResponse resp) throws IOException
    {
        Channel channel = findChannel(channelName);
        
        if(channel == null)
        {
            sendError(resp, "Channel not found: " + channelName);
            return;
        }
        
        try
        {
            if(!channel.isProcessing())
            {
                sendError(resp, "Channel is not running");
                return;
            }
            
            mChannelProcessingManager.stop(channel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Channel stopped: " + channelName);
            
            PrintWriter out = resp.getWriter();
            out.println(mGson.toJson(response));
            
            mLog.info("WebUI stopped channel: " + channelName);
        }
        catch(Exception e)
        {
            mLog.error("Error stopping channel: " + channelName, e);
            sendError(resp, "Error stopping channel: " + e.getMessage());
        }
    }

    private void createChannel(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        try
        {
            // Read JSON body
            BufferedReader reader = req.getReader();
            StringBuilder json = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                json.append(line);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> params = mGson.fromJson(json.toString(), Map.class);
            
            String name = (String) params.get("name");
            String type = (String) params.get("type");
            Number frequencyNum = (Number) params.get("frequency");
            
            if(name == null || name.trim().isEmpty())
            {
                sendError(resp, "Channel name is required");
                return;
            }
            
            if(type == null)
            {
                sendError(resp, "Decoder type is required");
                return;
            }
            
            if(frequencyNum == null)
            {
                sendError(resp, "Frequency is required");
                return;
            }
            
            long frequency = frequencyNum.longValue();
            
            // Check if channel already exists
            if(findChannel(name) != null)
            {
                sendError(resp, "Channel already exists: " + name);
                return;
            }
            
            // Create the channel
            Channel channel = new Channel(name);
            
            // Set decode configuration based on type
            DecodeConfiguration decodeConfig;
            DecoderType decoderType;
            
            switch(type.toUpperCase())
            {
                case "AM":
                    decodeConfig = new DecodeConfigAM();
                    decoderType = DecoderType.AM;
                    break;
                case "NBFM":
                case "FM":
                    decodeConfig = new DecodeConfigNBFM();
                    decoderType = DecoderType.NBFM;
                    break;
                default:
                    sendError(resp, "Unsupported decoder type: " + type + ". Supported: AM, NBFM/FM");
                    return;
            }
            
            channel.setDecodeConfiguration(decodeConfig);
            
            // Set source configuration (tuner)
            SourceConfigTuner sourceConfig = new SourceConfigTuner();
            sourceConfig.setFrequency(frequency);
            channel.setSourceConfiguration(sourceConfig);
            
            // Set optional parameters
            if(params.containsKey("system"))
            {
                channel.setSystem((String) params.get("system"));
            }
            if(params.containsKey("site"))
            {
                channel.setSite((String) params.get("site"));
            }
            
            // Add to model
            mChannelModel.addChannel(channel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Channel created: " + name);
            response.put("channel", createChannelInfo(channel));
            
            PrintWriter out = resp.getWriter();
            out.println(mGson.toJson(response));
            
            mLog.info("WebUI created channel: {} at {} Hz ({})", name, frequency, type);
        }
        catch(Exception e)
        {
            mLog.error("Error creating channel", e);
            sendError(resp, "Error creating channel: " + e.getMessage());
        }
    }

    private void deleteChannel(String channelName, HttpServletResponse resp) throws IOException
    {
        Channel channel = findChannel(channelName);
        
        if(channel == null)
        {
            sendError(resp, "Channel not found: " + channelName);
            return;
        }
        
        try
        {
            // Stop if running
            if(channel.isProcessing())
            {
                mChannelProcessingManager.stop(channel);
            }
            
            // Remove from model
            mChannelModel.removeChannel(channel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Channel deleted: " + channelName);
            
            PrintWriter out = resp.getWriter();
            out.println(mGson.toJson(response));
            
            mLog.info("WebUI deleted channel: " + channelName);
        }
        catch(Exception e)
        {
            mLog.error("Error deleting channel: " + channelName, e);
            sendError(resp, "Error deleting channel: " + e.getMessage());
        }
    }

    private void listDecoderTypes(HttpServletResponse resp) throws IOException
    {
        List<Map<String, String>> types = new ArrayList<>();
        
        // Only include simple decoder types suitable for web UI
        types.add(createTypeInfo("AM", "AM - Amplitude Modulation"));
        types.add(createTypeInfo("NBFM", "NBFM - Narrowband FM"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("types", types);
        
        PrintWriter out = resp.getWriter();
        out.println(mGson.toJson(response));
    }

    private Map<String, String> createTypeInfo(String value, String label)
    {
        Map<String, String> info = new HashMap<>();
        info.put("value", value);
        info.put("label", label);
        return info;
    }

    private Map<String, Object> createChannelInfo(Channel channel)
    {
        Map<String, Object> channelInfo = new HashMap<>();
        channelInfo.put("name", channel.getName());
        channelInfo.put("processing", channel.isProcessing());
        channelInfo.put("system", channel.getSystem());
        channelInfo.put("site", channel.getSite());
        
        if(channel.getSourceConfiguration() != null)
        {
            String sourceType = channel.getSourceConfiguration().getSourceType().toString();
            channelInfo.put("sourceType", sourceType);
            
            if(channel.getSourceConfiguration() instanceof SourceConfigTuner)
            {
                SourceConfigTuner tunerConfig = (SourceConfigTuner) channel.getSourceConfiguration();
                channelInfo.put("frequency", tunerConfig.getFrequency());
            }
        }
        
        if(channel.getDecodeConfiguration() != null)
        {
            channelInfo.put("decoderType", channel.getDecodeConfiguration().getDecoderType().toString());
        }
        
        return channelInfo;
    }

    private Channel findChannel(String name)
    {
        for(Channel channel : mChannelModel.getChannels())
        {
            if(channel.getName().equals(name))
            {
                return channel;
            }
        }
        return null;
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException
    {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        
        PrintWriter out = resp.getWriter();
        out.println(mGson.toJson(error));
    }
}
