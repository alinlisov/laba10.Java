package lab10.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/** Localized UI strings loaded from resources/location/messages_*.properties. */
public final class Text {
    private final Locale locale;
    private final ResourceBundle bundle;

    public Text(Locale locale) {
        this.locale = locale;
        this.bundle = ResourceBundle.getBundle("location.messages", locale);
    }

    public static Text ukrainian() { return new Text(Locale.forLanguageTag("uk")); }
    public static Text english() { return new Text(Locale.ENGLISH); }
    public static Text polish() { return new Text(Locale.forLanguageTag("pl")); }

    public Locale locale() { return locale; }
    public String get(String key) { return bundle.getString(key); }
    public String fmt(String key, Object... args) {
        Object[] printable = java.util.Arrays.stream(args)
                .map(value -> value instanceof Number ? value.toString() : value)
                .toArray();
        return new MessageFormat(get(key), locale).format(printable);
    }
}
