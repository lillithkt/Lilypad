# Lilypad

My custom osc client and chatbox!

## Features
- Modular, drag and drop your own modules!
- Highly Customizable
- Supports OSCQuery, so you can run this on separate hardware!
- A lock-based HTTP Server, for oAuth!
- Runs on Quest Standalone! (Or anything android)

### Modules
#### Core (Required for use)
- Core
- - Provides configuration for osc and logging
- GameStorage
- - VRChat log parser, and queries oscquery for the current avatar id
- Chatbox
- - Provides ChatboxModule, an extension of the module class that provides chatbox functionality, and the actual module manages the chatbox

#### Normal Modules
- AvatarPresets
- - Allows you to save and load avatar parameters!
- Banner
- - Adds a rotating banner to the top of the chatbox
- Spotify
- - Adds a now playing integration, which supports time-synced lyrics!
- Clock
- - Adds a clock to the chatbox


## To-Do
- [X] Run on android hardware
- - ~~Currently, there is an [Issue](https://github.com/hoijui/JavaOSC/issues/75) with JavaOSC, which does not let us run on android yet~~