package tech.wundero.placeholderredux.util;

import org.spongepowered.api.text.Text;

public class TextCharacter {

    private char c;
    private Text parent;

    public TextCharacter(char c, Text parent) {
        this.c = c;
        this.parent = parent;
    }

    public char getC() {
        return c;
    }

    public Text getParent() {
        return parent;
    }

}
