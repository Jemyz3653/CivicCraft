# CivicCraft Furniture 1.0.0

Minecraft Java 26.3 · Fabric Loader 0.19.5+ · Fabric API 0.163.0+ for 26.3 · Java 25 · Client + Server.

There are 342 furniture variants, with 26 wooden designs in 13 woods and four museum stanchions. The CivicCraft creative tab contains every variant. Recipes are available in Survival and unlock when the corresponding materials are obtained.

| Furniture | Use |
| --- | --- |
| Chairs, armchairs, stools, sofas, waiting room seats, benches | Right-click with an empty hand to sit. Press Shift to get up. |
| Tables, coffee tables, side tables, café tables | Place as decoration or combine into a larger surface. |
| Cabinets, counters, writing desks, dressers, kitchen units, filing cabinets and storage crates | Right-click to open 27 storage slots. |
| Bookcases, shop shelves and display racks | Store 27 stacks and display the first six slots on the model. |
| Wall shelves and market stands | Store 27 stacks and display the first three slots. |
| Display pedestals, glass display cases and book stands | Store 27 stacks and display the item in slot one. |
| Museum stanchions | Place matching colors in adjacent blocks to connect their ropes. |

Bar stools are sittable with the same controls as chairs. The museum stanchions are available in red, blue, black and white. The rope barrier blocks walking across a connected segment. Furniture faces the player during placement. Storage drops its contents when broken. English and Russian names are included.

For singleplayer, put the JAR and Fabric API in the client's `mods` folder. For multiplayer, install the same JAR and Fabric API on the server and all clients.

Use JDK 25 and run `gradlew.bat build` on Windows or `bash gradlew build` on Linux. The resource generator is `python tools/generate_resources.py`. For server feature checks run `gradlew.bat runGameTest`.

Run `python tools/draw_icon.py --curseforge CivicCraft-icon.png` with Pillow to regenerate the embedded icon and a 512px project logo.

License MIT. Created with ChatGPT by OpenAI.
