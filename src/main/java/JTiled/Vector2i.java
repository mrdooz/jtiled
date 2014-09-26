package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;

@XStreamAlias("vector2")

public class Vector2i {
    @XStreamAsAttribute
    int x;
    @XStreamAsAttribute
    int y;

    Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Vector2i add(int x, int y) {
        return new Vector2i(this.x + x, this.y + y);
    }

    Vector2i add(Vector2i v) {
        return new Vector2i(this.x + v.x, this.y + v.y);
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d", x, y);
    }

    static Vector2i ZERO = new Vector2i(0,0);
    static Vector2i ONE = new Vector2i(1,1);
}
