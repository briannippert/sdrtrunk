# Running SDRTrunk as a Background Service

## Quick Start

### Start SDRTrunk in background
```bash
./run.sh
```

### Check if running
```bash
./status.sh
```

### Stop SDRTrunk
```bash
./stop.sh
```

## Detailed Usage

### Starting

The `run.sh` script will:
1. Check if Java 24 is available
2. Start SDRTrunk in the background
3. Save the process ID to `/tmp/sdrtrunk.pid`
4. Redirect output to `/tmp/sdrtrunk.log`
5. Report success or failure

Example:
```bash
$ ./run.sh
Using Java 24 from: /tmp/jdk-24.0.1-full
Starting SDRTrunk in background...
Log file: /tmp/sdrtrunk.log
PID file: /tmp/sdrtrunk.pid
Web UI will be available at: http://localhost:8080

✓ SDRTrunk started successfully (PID: 12345)

To view logs:  tail -f /tmp/sdrtrunk.log
To stop:       ./stop.sh
```

### Viewing Logs

Real-time log viewing:
```bash
tail -f /tmp/sdrtrunk.log
```

Last 100 lines:
```bash
tail -n 100 /tmp/sdrtrunk.log
```

Search logs:
```bash
grep "WebStream" /tmp/sdrtrunk.log
```

### Checking Status

```bash
$ ./status.sh
=========================================
  SDRTrunk Status
=========================================

Status: RUNNING ✓
PID: 12345
Process Info:  1.5  2.3 00:05:23 java...

Files:
  PID file: /tmp/sdrtrunk.pid
  Log file: /tmp/sdrtrunk.log

Web UI: http://localhost:8080

Commands:
  View logs:  tail -f /tmp/sdrtrunk.log
  Stop:       ./stop.sh

Web Server: RESPONDING ✓
```

### Stopping

The `stop.sh` script will:
1. Try graceful shutdown (SIGTERM)
2. Wait up to 10 seconds
3. Force kill if necessary (SIGKILL)

```bash
$ ./stop.sh
Stopping SDRTrunk (PID: 12345)...
✓ SDRTrunk stopped successfully
```

## Files

| File | Purpose |
|------|---------|
| `/tmp/sdrtrunk.pid` | Process ID of running instance |
| `/tmp/sdrtrunk.log` | Application output and logs |

## Troubleshooting

### "Already running" error
```bash
$ ./run.sh
SDRTrunk is already running (PID: 12345)
Use ./stop.sh to stop it first
```

**Solution:** Stop the existing instance first:
```bash
./stop.sh
./run.sh
```

### Can't stop the process
```bash
# Manually kill the process
kill -9 $(cat /tmp/sdrtrunk.pid)
rm /tmp/sdrtrunk.pid
```

### Check what's using port 8080
```bash
lsof -i :8080
# or
netstat -tlnp | grep 8080
```

### Web server not responding
Check the logs for errors:
```bash
tail -f /tmp/sdrtrunk.log | grep -i "error\|exception"
```

## Running on System Startup

### Using systemd (Linux)

Create `/etc/systemd/system/sdrtrunk.service`:
```ini
[Unit]
Description=SDRTrunk Radio Scanner
After=network.target

[Service]
Type=forking
User=brian
WorkingDirectory=/home/brian/Documents/GitHub/sdrtrunk
ExecStart=/home/brian/Documents/GitHub/sdrtrunk/run.sh
ExecStop=/home/brian/Documents/GitHub/sdrtrunk/stop.sh
PIDFile=/tmp/sdrtrunk.pid
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable sdrtrunk
sudo systemctl start sdrtrunk
sudo systemctl status sdrtrunk
```

### Using cron (@reboot)

Add to crontab:
```bash
crontab -e
```

Add this line:
```
@reboot cd /home/brian/Documents/GitHub/sdrtrunk && ./run.sh
```

## Advanced Usage

### Custom log location
```bash
# Edit run.sh and change:
LOG_FILE="/tmp/sdrtrunk.log"
# to:
LOG_FILE="$HOME/sdrtrunk.log"
```

### Multiple instances
To run multiple instances on different ports, you'll need to:
1. Modify the port in `SDRTrunk.java`
2. Use different PID and log files
3. Ensure different tuner configurations

### Log rotation
Prevent logs from growing too large:
```bash
# Add to crontab
0 0 * * * [ -f /tmp/sdrtrunk.log ] && tail -1000 /tmp/sdrtrunk.log > /tmp/sdrtrunk.log.tmp && mv /tmp/sdrtrunk.log.tmp /tmp/sdrtrunk.log
```

## Security Notes

⚠️ **Important:**
- PID and log files are in `/tmp` (world-readable)
- No authentication on web interface
- Runs with user permissions
- Consider using systemd for production deployments

For production use:
1. Move files to user directory (`~/.sdrtrunk/`)
2. Set proper permissions (600)
3. Configure firewall rules
4. Consider adding authentication
