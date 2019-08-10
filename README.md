# Rune Light - The safer client.

Its RuneLitePlus but it only runs Local-Injected without a Launcher.

- Loot Tracker works without API.
- XP Tracker works without API.
- Slayer plugin works better without API.
- Chat Commands has had some commands removed (No API).
- Raids Plugin works without the Party aspect.
- Grand Exchange Shows osBuddy average on search.
- Item Price plugin shows osBuddy price if higher.
- GroundItems plugin shows osBuddy price if higher.

[You have to use your own local HTTP-Service to use some features](https://github.com/Hermetism/runelight/wiki/SETUP-YOUR-OWN-HTTP-SERVICE)

You can use it without if you don't mind missing out on:
- WorldHopper (in-game still works obviously)
- Osbuddy Prices

## Project Layout
- [cache](cache/src/main/java/net/runelite/cache) - Libraries used for reading/writing cache files, as well as the data in it
- [deobfuscator](deobfuscator/src/main/java/net/runelite/deob) - Can decompile and cleanup gamepacks as well as map updates to newer revs
- [http-api](http-api/src/main/java/net/runelite/http/api) - RuneLight Minimal HTTP-API
- [http-service](http-service/src/main/java/net/runelite/http/service) - RuneLight Minimal HTTP Service
- [injector-plugin](injector-plugin/src/main/java/net/runelite/injector) - RuneLiteExtended Injector
- [runelite-api](runelite-api/src/main/java/net/runelite/api) - RuneLite API, Interfaces for accessing the client
- [runelite-mixins](runelite-mixins/src/main/java/net/runelite) - RuneLite/RuneLiteExtended Mixins.
- [runescape-api](runescape-api/src/main/java/net/runelite) - Mappings correspond to these interfaces, runelite-api is a subset of this
- [runelite-client](runelite-client/src/main/java/net/runelite/client) - Game client with plugins
- [helpers](runelite-client/src/main/java/net/runelite/client/plugins/helpers) - HELPER - RuneLight LEGO Plugin Maker


## HELPER PLUGIN
Most of Helper Plugins best features work with the [STRETCHED MODE with these settings](https://i.imgur.com/eY9AlTD.png)

There are **MANY** other features but some of them are listed below.

- HELPER is *Static*, so you can use it in any plugin with VERY little effort.
- Move mouse using Fitts Law (Commented Out)
- Move mouse using Berstein Polynominals (Commented Out, inaccurate)
- Mouse mouse using [Natural Mouse (RS Version)](https://github.com/Hermetism/Natural-Mouse-RS)
- Mark bank booths in entire regions
- Manage and Compare Inventory and Bank (some deprecated)
- Find random area to click on Widgets and WidgetItems
- Find tile area (1 tile or several at once)
- Example Generics/Enums
- I'm really under selling this go [read everything here](runelite-client/src/main/java/net/runelite/client/plugins/helpers).

       
## NaturalMouseMotion RS
Contains custom flow templates and factory templates for Old School Runescape

- Factory rs - Move at Normal Speed
- Factory rsf - Move Fast
- you will need this to use Helper [Natural-Mouse-RS](https://github.com/Hermetism/Natural-Mouse-RS) unless you want to make your own
- All random flows are 125 doubles in len
- randomFlowLow() - between (1) and (50) 
- randomFlowLowest() -between (1) and (25)
- randomFlowHigh() -  between (50) and (90) 
- randomFlowWide() - between (1) and (110) 
- randomDouble(double min, double max) - Random double in range
- The default natural mouse random() and others are also included.


