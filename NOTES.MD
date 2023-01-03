
### Good 1.18 tutorials
- https://www.mcjty.eu/docs/1.18/
- https://github.com/McJty/TutorialV3/tree/98b824b6adfb318c19fe96ae41f504ade7d40dcf/src/main/java/com/example/tutorialv3/manasystem
- https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.18.1/tree/main/src/main/java/net/kaupenjoe/tutorialmod
- https://moddingtutorials.org/1.18.2/environment-setup
- Entity creation in blockbench https://www.youtube.com/watch?v=XBIXn7s-fMo

### Capabilities
- https://github.com/mickelus/tetra/search?q=capability/
- https://github.com/Shadows-of-Fire/Apotheosis/search?q=capability
  //
### Item Stack
- https://www.youtube.com/watch?v=edcpW1zzUfk

### Other Random Links
- https://www.youtube.com/watch?v=prqMxqUtj-0
- Teleport: https://moddingtutorials.org/1.19.2/advanced-items/
- Loading values from config: https://www.youtube.com/watch?v=QN9jq3_V-I8

###
- Particles
    - https://forge.gemwire.uk/wiki/Particles
    - https://www.youtube.com/watch?v=DM69u76tXMI

###
- Hit Detection
    - https://github.com/ZsoltMolnarrr/BetterCombat/tree/1.18.2/forge
    - https://github.com/ZsoltMolnarrr/BetterCombat/blob/74ccd5f7092b932b26462f27e0ea383acc1a92ae/common/src/main/java/net/bettercombat/client/collision/TargetFinder.java

### Helpful commands
- ```/setblock ~ ~ ~ testmod:inscription_table```
- ```/data get entity @s```
- ```/data get entity @s SelectedItem```
- ```/data get entity @s ForgeCaps```

### Give Generator
- https://mapmaking.fr/give1.16/
- ```give @s minecraft:iron_chestplate{AttributeModifiers:[{AttributeName:"testmod:cooldown_reduction",Slot:"chest",Amount:.2,Operation:1,UUID:[I;109,10810,109635,18212]}]} 1```
- ```give @s minecraft:iron_chestplate{AttributeModifiers:[{AttributeName:"testmod:max_mana",Slot:"chest",Amount:.2,Operation:1,UUID:[I;109,10810,109635,18212]}]} 1```

### Notes
- Block Overlay Example: https://github.com/Hexeption/DarkForge/blob/master/src/main/java/uk/co/hexeption/darkforge/mod/mods/render/BlockOverlay.java
- Nice rendering library: https://github.com/maruohon/malilib
- https://github.com/TobyNguyen710/kyomod/blob/56d3a9dc6b45f7bc5ecdb0d6de9d201cea2603f5/Mod/build/tmp/expandedArchives/forge-1.19.2-43.1.7_mapped_official_1.19.2-sources.jar_b6309abf8a7e6a853ce50598293fb2e7/net/minecraft/world/entity/projectile/ShulkerBullet.java
- https://github.com/maximumpower55/Aura/blob/1.18/src/main/java/me/maximumpower55/aura/entity/SpellProjectileEntity.java
- https://github.com/CammiePone/Arcanus/blob/1.18-dev/src/main/java/dev/cammiescorner/arcanus/common/entities/MagicMissileEntity.java#L51
- https://github.com/maximumpower55/Aura
- Projectile.lerpMotion is also a decent reference
This method appears to set the movement delta (change in position) of the projectile, as well as its rotation based on the movement delta.
The method takes three parameters: p37279, p37280, and p37281, which represent the movement delta in the x, y, and z directions, respectively.
The method first sets the movement delta using the setDeltaMovement method. It then checks if the xRotO and yRotO fields are equal to 0.0. If they are,
it calculates the pitch and yaw (rotation around the x and y axes, respectively) based on the movement delta using the atan2 function. It then sets the
pitch and yaw of the projectile using the setXRot and setYRot methods, and stores the pitch and yaw in the xRotO and yRotO fields, respectively. Finally,
it calls the moveTo method to update the position and rotation of the projectile.

