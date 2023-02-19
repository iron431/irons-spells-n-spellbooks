##### Jigsaw Blocks
- target pool: testmod:basic_dungeon
    - basically the name of the data pack?
- name: name of this connector jigsaw
- target name: names of eligible connector jigsaws that it can be connected to

##### Structure Blocks
- General
    - place at the NW corner of the structure to save
    - change lower left button to save (click twice)
    - Set: Show Invisible Blocks to ON
        - Air: Blue, Void: Pink

##### Approach
- dungeon_pool (only contains the origin structure)
- hallway_pool (jigsaw target name: room_or_hallway)
- room_or_hallway_pool (jigsaw target name: room_or_hallway)
- room_pool (jigsaw target name: room_or_hallway)
- secret_room_pool (jigsaw target name: secret_room)
- treasure_or_mob_pool (jigsaw target name: treasure_or_mob)
- treasure_pool (jigsaw target name: treasure_or_mob)
- mob_pool (jigsaw target name: treasure_or_mob)

##### Structures
- Core
    - testmod:dungeon_origin
- Rooms
    - testmod:room_origin_1 (4 connection points)
    - testmod:room_origin_2 (3 connection points)
    - testmod:room_1 (necromancer with wooden platform, 0 connection points)
    - testmod:room_2 (zig zag with dead end, 0 connection points)
    - testmod:room_3 (zig zag, 1 hallway connection point)
- Secret Rooms
    - testmod:secret_room_1 (dead end, two utility spawnpoints)
- Halls
    - testmod:hallway_straight
    - testmod:hallway_tee (two connection points, one secret room point)
    - testmod:hallway_turn_1
    - testmod:hallway_turn_2
- Utility (Treasure/Mobs)
    - testmod:chest_1 (single chest)
- Other
    - testmod:end_room_wall (can be used to end a chain where nothing else will fit)
    - testmod:end_hall_wall (can be used to end a chain where nothing else will fit)