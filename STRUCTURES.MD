##### Links
- https://www.youtube.com/watch?v=BIa_gqc93ok
- https://minecraft.fandom.com/wiki/Custom_structure
- 

##### Jigsaw Blocks
- target pool: testmod:basic_dungeon
    - basically the name of the data pack?
- name: name of this connector jigsaw. this is used by other jigsaw pieces looking for a match
- target name: this block will look for other blocks that match this in the target pool

##### Structure Blocks
- General
    - place at the NW corner of the structure to save
    - change lower left button to save (click twice)
    - Set: Show Invisible Blocks to ON
        - Air: Blue, Void: Pink

##### Approach
- Target pools
- Name and Target Name on a Jigsaw block can be blank to prevent doing a lookup or being looked up 

##### Pools
- dungeon1_start_pool (only contains the origin structure)
- dungeon1_hallway_pool (jigsaw target name: room_or_hallway)
- dungeon1_room_or_hallway_pool (jigsaw target name: room_or_hallway)
- dungeon1_room_origin_pool (jigsaw target name: room_origin)
- dungeon1_room_pool (jigsaw target name: room_or_hallway)
- dungeon1_secret_room_pool (jigsaw target name: secret_room)
- dungeon1_treasure_or_mob_pool (jigsaw target name: treasure_or_mob)
- dungeon1_treasure_pool (jigsaw target name: treasure_or_mob)
- dungeon1_mob_pool (jigsaw target name: treasure_or_mob)

##### Structures
- Core
    - testmod:dungeon_origin
- Rooms
    - testmod:room_origin_1 (4 connection points)
    - testmod:room_origin_2 (3 connection points)
    - testmod:room_1 (necromancer with wooden platform, 0 connection points)
    - testmod:room_2 (zig zag with dead end, 0 connection points)
    - testmod:room_3 (zig zag, 1 hallway connection point)
    - testmod:room_4 (giant jail, 0 hallway connection points)
- Secret Rooms
    - testmod:secret_room_1 (dead end, two utility spawnpoints)
- Halls
    - testmod:hallway_straight
    - testmod:hallway_tee (two connection points, one secret room point)
    - testmod:hallway_turn_1
    - testmod:hallway_turn_2
- Utility (Treasure/Mobs)
  - testmod:chest_1 (single chest)
    - testmod:chest_horiz (single chest)
- Other
    - testmod:end_room_wall (can be used to end a chain where nothing else will fit)
    - testmod:end_hall_wall (can be used to end a chain where nothing else will fit)

#####
testmod:dungeon1_start_pool
testmod:dungeon1_room_or_hallway_pool
testmod:dungeon1_hallway_pool
testmod:dungeon1_room_pool
testmod:dungeon1_treasure_pool
testmod:dungeon1_secret_room_pool

testmod:room_or_hallway
testmod:treasure_or_mob
testmod:secret_room

minecraft:stone_bricks
