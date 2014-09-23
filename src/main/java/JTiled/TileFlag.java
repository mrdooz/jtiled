package JTiled;

class WallFlag {
    static int Left     = 1 << 0;
    static int Top      = 1 << 1;
    static int Right    = 1 << 2;
    static int Bottom   = 1 << 3;
}

public enum TileFlag {
    None,

    TopLeft,
    TopMiddle,
    TopRight,
    MiddleLeft,
    Middle,
    MiddleRight,
    BottomRight,
    BottomMiddle,
    BottomLeft,
}
