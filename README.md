# Chatter14

![Stable](https://img.shields.io/badge/Stable-0.5.0-blue?style=for-the-badge "Stable version 0.5.0")

Provides a way to extract Final Fantasy XIV chat conversations from an Advanced Combat
Tracker (ACT) log.

## Contents

 - [Introduction](#introduction)
 - [Installation](#installation)

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

## <a name="installation">Installation</a>

### Windows

The Windows version is pacakged as an installer, just run the installer and it should
install everything needed. This product requires Java to run and it should install the
required files automatically but if it cannot it will prompt you to install the Java
runtime.

### Linux, MacOS

Extract the tar file into an appropriate place and add the `chatter14` script to the
PATH.

## Acknowlegements and Licenses

These are the packages used in this project and their licenses.

### Libraries

#### 4koma Toml parser

> See [LICENSE.md](https://github.com/valderman/4koma/blob/main/LICENSE) for the full
> texts of the licenses.

#### Clickt arguments processor

> See [LICENSE.md](https://github.com/ajalt/clikt/blob/master/LICENSE.txt) for the full
> texts of the licenses.

#### Hack font

> **Hack** work is &copy; 2018 Source Foundry Authors. MIT License
>
> **Bitstream Vera Sans Mono** &copy; 2003 Bitstream, Inc. (with Reserved Font Names _
> Bitstream_ and _Vera_). Bitstream Vera License.
>
> See [LICENSE.md](https://github.com/source-foundry/Hack/blob/master/LICENSE.md) for the
> full texts of the licenses.

### Build Tools

#### gradle-release plugin

> See [LICENSE.md](https://github.com/researchgate/gradle-release/blob/main/LICENSE) for
> the full texts of the licenses.

#### Launch4j

> See [launch4j page](http://launch4j.sourceforge.net/index.html) for links to the
> licenses.

### Graphics

#### Cat Shadows images

> Hello!
>
> This is a Cat Shadow Icon Set from Iconka.com
> It contains icons of cats, in sizes 64x64, 128x128, 256x256.
> Format: PNG with transparent background.
>
> This icons a free to use for non-commercial or commercial purposes.
> You can use them in your websites, applications, or any other end product.
> You can't sell or copyright them, or use them in templates intended for sale, or sell or
> copyright derivatives from the icons.
> Enjoy and thank you for your interest!
