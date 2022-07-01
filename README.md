# Chatter14

![Stable](https://img.shields.io/badge/Stable-0.5.0-blue?style=for-the-badge "Stable version 0.5.0")

Provides a way to extract Final Fantasy XIV chat conversations from an Advanced Combat
Tracker (ACT) log.

## Contents

- [Introduction](#introduction)
- [Usage](#usage)
    - [Window](#window)
    - [Command Line](#command)
    - [Configuration File](#configuration)
- [Installation](#installation)
    - [Windows](#installation-windows)
    - [Linux, MacOS](#installation-linux)
- [Developing](#developing)
- [Acknowlegements and Licenses](#installation-acknowledgement)

## <a name="introduction">Introduction</a>

ACT is often used to view information about combat but it also extracts
all of the chat logs as well. However these chat logs are interspersed with all kinds of
other information about actions happening in the environment around your character. This
makes reading a conversation nearly impossible. And if there are multiple separate
conversations at the same time these conversations are interspersed.

This app will extract chats from the ACT logs and will further strip out all but a single
group of people that you define. It can also, optionally include all of the emotes that
were used between the members of the group as many conversations occur almost entirely
through emotes.

The current version treats all chat types (say, yell, linkshell, etc.) the same and
outputs them all. A future version will let you select which chat types to include in
a particular conversation. I personal have noted that people are not consistent in the
use of chat types even for a single conversation, switching between a linkshell, say, and
even yell.

## <a name="usage">Usage</a>

Chatter14 can be run from the command line or as a window that accepts drag-and-drop.

### <a name="window">Window</a>

Starting Chatter14 with no arguments or clicking on the icon will start Chatter14 in
windowed mode.

<img src="src/main/docs/main.png">

To convert one or more ACT log files drag them to the Chatter14 window. They will be
converted using the current options in the window. The generated files will be .txt files
in the same directory as the logs, with the a name in the form
{original_name}-{group_short_name}.txt. It will include all of the lines from the log that
match the group definition and optionally the emotes.

#### Options

The dropdown selects the group of people to filter for. These are defined in the
[configuration file](#configuration).

<img src="src/main/docs/main_drop.png">

The **Emotes** checkbox selects whether the emote entries for the group are included in
the output file. Many "conversations" in **FFXIV** are carried out in a combination of
chat
and emotes.

The **Replace files** checkbox determines whether any existing generated files will be
replaced. If this is not checked, any existing files will be skipped.

If the **Dry run** checkbox is checked then `Chatter14` will do everything except write
out the output files. Mostly useful for testing the program.

The **Log** button will bring a window that shows the execution log. Mostly useful for
testing but it also helps to show progress.

### <a name="command">Command Line</a>

### <a name="configuration">Configuration File</a>

There is a special group that is always present
called `Everyone` that selects all conversations from every person in the log. Thi

## <a name="installation">Installation</a>

### <a name="installation-windows">Windows</a>

The Windows version is pacakged as an installer, just run the installer and it should
install everything needed. This product requires Java to run and it should install the
required files automatically but if it cannot it will prompt you to install the Java
runtime.

### <a name="installation-linux">Linux, MacOS</a>

Extract the tar file into an appropriate place and add the `chatter14` script to the
PATH.

## <a name="developing">Developing</a>

### Build Windows icon

The icon is generated from and JPG using the ImageMagick tool.

```command
magick src\main\resources\images\chatter14.jpg -background none -extent 256x256 -define icon:auto-resize="256,128,96,64,48,32,16" src\main\installer\chatter14.ico
```

## <a name="installation-acknowledgement">Acknowlegements and Licenses</a>

Licenses are listed here [Licenses](LICENSES.md).

