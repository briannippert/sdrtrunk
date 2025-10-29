![Gradle Build](https://github.com/dsheirer/sdrtrunk/actions/workflows/gradle.yml/badge.svg)
![Nightly Release](https://github.com/dsheirer/sdrtrunk/actions/workflows/nightly.yml/badge.svg)

# sdrtrunk
A cross-platform java application for decoding, monitoring, recording and streaming trunked mobile and related radio protocols using Software Defined Radios (SDR).

* [Help/Wiki Home Page](https://github.com/DSheirer/sdrtrunk/wiki)
* [Getting Started](https://github.com/DSheirer/sdrtrunk/wiki/Getting-Started)
* [User's Manual](https://github.com/DSheirer/sdrtrunk/wiki/User-Manual)
* [Download](https://github.com/DSheirer/sdrtrunk/releases)
* [Support](https://github.com/DSheirer/sdrtrunk/wiki/Support)

![sdrtrunk Application](https://github.com/DSheirer/sdrtrunk/wiki/images/sdrtrunk.png)
**Figure 1:** sdrtrunk Application Screenshot

## Download the Latest Release
All release versions of sdrtrunk are available from the [releases](https://github.com/DSheirer/sdrtrunk/releases) tab.

* **(alpha)** These versions are under development feature previews and likely to contain bugs and unexpected behavior.
* **(beta)** These versions are currently being tested for bugs and functionality prior to final release.
* **(final)** These versions have been tested and are the current release version.

## Download Nightly Software Build
The [nightly](https://github.com/DSheirer/sdrtrunk/releases/tag/nightly) release contains current builds of the software 
for all supported operating systems.  This version of the software may contain bugs and may not run correctly.  However, 
it let's you preview the most recent changes and fixes before the next software release.  **Always backup your 
playlist(s) before you use the nightly builds.**  Note: the nightly release is updated each time code changes are 
committed to the code base, so it's not really 'nightly' as much as it is 'current'.

## Minimum System Requirements
* **Operating System:** Windows (~~32 or~~ 64-bit), Linux (~~32 or~~ 64-bit) or Mac (64-bit, 12.x or higher)
* **CPU:** 4-core
* **RAM:** 8GB or more (preferred).  Depending on usage, 4GB may be sufficient.

## Linux Setup: USB Device Permissions (udev Rules)

On Linux systems, you need to install udev rules to allow non-root access to SDR USB devices. Without these rules, you'll get a **"No Tuner Available"** error when trying to start a channel.

### Quick Installation

Install all SDR device udev rules:

```bash
sudo cp src/main/resources/*.rules /etc/udev/rules.d/
sudo udevadm control --reload-rules
sudo udevadm trigger
```

Then **unplug and replug your SDR device** (or reboot).

### Included udev Rules

The following udev rules files are provided in `src/main/resources/`:

- **rtl-sdr.rules** - RTL-SDR dongles (RTL2832U-based devices)
- **52-airspy.rules** - Airspy devices
- **53-hackrf.rules** - HackRF devices  
- **funcube-dongle.rules** - FunCube Dongle Pro/Pro+

### Verification

After installation, verify your device has proper permissions:

```bash
# Find your device
lsusb | grep -iE "rtl|airspy|hackrf|funcube"

# Check permissions (should show crw-rw-rw- or similar)
ls -la /dev/bus/usb/XXX/YYY
```

The device should be readable and writable by your user. You should already be a member of the `plugdev` group on most systems.
