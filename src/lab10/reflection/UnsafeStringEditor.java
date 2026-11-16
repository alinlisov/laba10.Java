package lab10.reflection;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * An EDUCATIONAL demonstration, not an appropriate production technique.
 * On Java 8 String.value is char[]; on Java 9+ it is usually byte[] (compact strings).
 * We mutate the private backing array IN PLACE so no final fields are assigned.
 * The replacement must have the same representation (coder and backing length).
 * Requires: --add-opens=java.base/java.lang=ALL-UNNAMED on JDK 17+.
 */
public final class UnsafeStringEditor {
    private UnsafeStringEditor() {}

    public static String storageType() throws ReflectiveOperationException {
        Field field = String.class.getDeclaredField("value");
        return field.getType().getSimpleName();
    }

    public static void replaceContents(String target, String replacement)
            throws ReflectiveOperationException {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(replacement, "replacement");
        if (target == replacement) return;
        Field valueField = String.class.getDeclaredField("value");
        valueField.setAccessible(true);
        Object original = valueField.get(target);
        Object proposed = valueField.get(replacement);

        if (original instanceof char[] originalChars && proposed instanceof char[] replacementChars) {
            if (originalChars.length != replacementChars.length) {
                throw new IllegalArgumentException("Replacement must have the same number of UTF-16 code units");
            }
            System.arraycopy(replacementChars, 0, originalChars, 0, originalChars.length);
        } else if (original instanceof byte[] originalBytes && proposed instanceof byte[] replacementBytes) {
            Field coderField = String.class.getDeclaredField("coder");
            coderField.setAccessible(true);
            byte originalCoder = coderField.getByte(target);
            byte replacementCoder = coderField.getByte(replacement);
            if (originalCoder != replacementCoder || originalBytes.length != replacementBytes.length) {
                throw new IllegalArgumentException("Different internal String encoding/length; choose a replacement with equal character length and compatible script");
            }
            System.arraycopy(replacementBytes, 0, originalBytes, 0, originalBytes.length);
        } else {
            throw new IllegalStateException("Unsupported String backing array type: " + original.getClass());
        }
        // String is meant to be immutable. Reset hash caches when present;
        // this still does not make reflective mutation safe for production use.
        try {
            Field hash = String.class.getDeclaredField("hash");
            hash.setAccessible(true);
            hash.setInt(target, 0);
        } catch (NoSuchFieldException ignored) { /* older JDK layout */ }
        try {
            Field hashIsZero = String.class.getDeclaredField("hashIsZero");
            hashIsZero.setAccessible(true);
            hashIsZero.setBoolean(target, false);
        } catch (NoSuchFieldException ignored) { /* older JDK layout */ }
    }
}
