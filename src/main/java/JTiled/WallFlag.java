package JTiled;

// If a wall flag is set, it means that the tile in that direction is of the same terrain
class WallFlag {
//    static long Left     = 0;
//    static long Top      = 3;
//    static long Right    = 6;
//    static long Bottom   = 9;

    static long W       = 0;
    static long NW      = 3;
    static long N       = 6;
    static long NE      = 9;
    static long E       = 12;
    static long SE      = 15;
    static long S       = 18;
    static long SW      = 21;

    static long Inner    = 0x1;
    static long Outer    = 0x2;
    static long Border   = 0x4;
    static long DontCare = 0x7;

    static Vector2i[] dir = {
            new Vector2i(-1, +0),
            new Vector2i(-1, -1),
            new Vector2i(+0, -1),
            new Vector2i(+1, -1),
            new Vector2i(+1, +0),
            new Vector2i(+1, +1),
            new Vector2i(+0, +1),
            new Vector2i(-1, +1),
    };

    static long[] mask = {
            0b111 << 0,
            0b111 << 3,
            0b111 << 6,
            0b111 << 9,
            0b111 << 12,
            0b111 << 15,
            0b111 << 18,
            0b111 << 21,
    };
}
