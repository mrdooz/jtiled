package JTiled;

// If a wall flag is set, it means that the tile in that direction is of the same terrain
class WallFlag {
    static long Left     = 0;
    static long Top      = 3;
    static long Right    = 6;
    static long Bottom   = 9;

    static long Inner    = 0x1;
    static long Outer    = 0x2;
    static long Border   = 0x4;
    static long DontCare = 0x7;
}
