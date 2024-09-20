# Lilypad

My custom osc client and chatbox!

## Features
- Modular, drag and drop your own modules!
- Highly Customizable
- Supports OSCQuery, so you can run this on separate hardware!
- A lock-based HTTP Server, for oAuth!
- Runs on android hardware, for standalone users!
- - This is untested! Please [Open An Issue](https://github.com/imlvna/Lilypad/issues/new) if you have any issues!

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