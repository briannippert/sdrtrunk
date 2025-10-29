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
import io.github.dsheirer.controller.channel.ChannelEvent;
import io.github.dsheirer.controller.channel.ChannelModel;
import io.github.dsheirer.controller.channel.ChannelProcessingManager;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        else
        {
            sendError(resp, "Invalid action or missing channel parameter");
        }
    }

    private void listChannels(HttpServletResponse resp) throws IOException
    {
        List<Map<String, Object>> channelList = new ArrayList<>();
        
        for(Channel channel : mChannelModel.getChannels())
        {
            Map<String, Object> channelInfo = new HashMap<>();
            channelInfo.put("name", channel.getName());
            channelInfo.put("processing", channel.isProcessing());
            channelInfo.put("system", channel.getSystem());
            channelInfo.put("site", channel.getSite());
            
            // Get frequency if available
            if(channel.getSourceConfiguration() != null)
            {
                String sourceType = channel.getSourceConfiguration().getSourceType().toString();
                channelInfo.put("sourceType", sourceType);
            }
            
            channelList.add(channelInfo);
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
