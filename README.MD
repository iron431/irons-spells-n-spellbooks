# The Mod

## Info
### Requirements and Concepts
- https://docs.google.com/document/d/1IKr9KUGlv01eKqjDgKikAks-1RKGPKXncFk2uGoRORM/edit

### Good 1.18 tutorials
- https://www.mcjty.eu/docs/1.18/
- https://github.com/McJty/TutorialV3/tree/98b824b6adfb318c19fe96ae41f504ade7d40dcf/src/main/java/com/example/tutorialv3/manasystem

### Capabilities
- https://github.com/mickelus/tetra/search?q=capability/
- https://github.com/Shadows-of-Fire/Apotheosis/search?q=capability

### Item Stack
- https://www.youtube.com/watch?v=edcpW1zzUfk

### Other Random Links
- https://www.youtube.com/watch?v=prqMxqUtj-0

### Helpful commands
- ```/setblock ~ ~ ~ testmod:inscription_table```
- ```/data get entity @s```
- ```/data get entity @s SelectedItem```
- ```/data get entity @s ForgeCaps```

### Give Generator
- https://mapmaking.fr/give1.16/
- ```give @s minecraft:iron_chestplate{AttributeModifiers:[{AttributeName:"testmod:cooldown_reduction",Slot:"chest",Amount:.2,Operation:1,UUID:[I;109,10810,109635,18212]}]} 1```
- ```give @s minecraft:iron_chestplate{AttributeModifiers:[{AttributeName:"testmod:max_mana",Slot:"chest",Amount:.2,Operation:1,UUID:[I;109,10810,109635,18212]}]} 1```

### Dimension Data Storage (from Chat GPT)
In the context of Minecraft modding, the DimensionDataStorage class is a class that is used to store data specific to a dimension in the game. In Minecraft, dimensions are separate, self-contained worlds that can have different properties and features. The DimensionDataStorage class allows mod developers to create and store data that is specific to a particular dimension, which can be used to customize the behavior of their mod in that dimension.

For example, a mod might use the DimensionDataStorage class to store information about the state of certain objects or structures in a particular dimension, or to track the progress of a player through a dimension. The data stored in the DimensionDataStorage class can be used to customize the gameplay experience for players in that dimension, or to provide additional features or functionality for players in that dimension.

It's worth noting that the DimensionDataStorage class is part of the Minecraft Forge modding framework, which is a widely used toolkit for creating mods for Minecraft. If you are interested in modding Minecraft and want to learn more about the DimensionDataStorage class, you may want to start by learning more about the Minecraft Forge framework and how it can be used to create mods for Minecraft.