package link.alpinia.Jasper.util;

import link.alpinia.Jasper.Jasper;

import java.awt.Color;

/**
 * EmbedUI Class
 * @author dumbass/laika
 * @apiNote Another mediocre thing dumbass made, its literally just public final static variables.
 */
public abstract class EmbedUI {

    // Strings
    public static String BRAND = Jasper.instance.footer;
    public static final String RESPONSE_PRIVILEGES = " Insufficient privileges.";

    // Colors
    public static final Color SUCCESS = new Color(136,176,75);
    public static final Color FAILURE = new Color(255,111,97);
    public static final Color INFO = new Color(123,196,196);
}
