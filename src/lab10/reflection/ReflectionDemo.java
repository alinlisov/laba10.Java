package lab10.reflection;

import lab10.i18n.Text;
import java.lang.reflect.InaccessibleObjectException;
import java.util.Scanner;

/** Shows the two required initialization methods: literal and keyboard input. */
public final class ReflectionDemo {
    private ReflectionDemo() {}

    public static void run(Scanner input, Text text) {
        System.out.println("\n" + text.get("reflection.title"));
        System.out.println(text.get("reflection.warning"));
        try {
            System.out.println(text.fmt("reflection.storage", UnsafeStringEditor.storageType()));
            // Unique literal so we do not accidentally corrupt common interned values.
            String literal = "Lab10ABC";   // 8 Latin-1 chars (unique for this lab)
            System.out.println(text.fmt("reflection.literalBefore", literal));
            try {
                UnsafeStringEditor.replaceContents(literal, "Changed!"); // 8 chars
                System.out.println(text.fmt("reflection.literalAfter", literal));
            } finally {
                // Undo the experiment: a literal is interned and can affect other code.
                // Create the restore value from fresh chars rather than the modified literal.
                String original = new String(new char[]{'L','a','b','1','0','A','B','C'});
                UnsafeStringEditor.replaceContents(literal, original);
            }

            System.out.println(text.get("reflection.input"));
            String typed = input.nextLine();
            System.out.println(text.get("reflection.replacement"));
            String replacement = input.nextLine();
            System.out.println(text.fmt("reflection.inputBefore", typed));
            try {
                UnsafeStringEditor.replaceContents(typed, replacement);
                System.out.println(text.fmt("reflection.inputAfter", typed));
            } catch (IllegalArgumentException ex) {
                System.out.println(text.get("reflection.lengthError"));
                System.out.println(text.fmt("reflection.inputAfter", typed));
            }
        } catch (InaccessibleObjectException | IllegalAccessException ex) {
            System.out.println(text.get("reflection.jvmFlagError"));
        } catch (ReflectiveOperationException ex) {
            System.out.println(text.fmt("reflection.failed", ex.getMessage()));
        }
    }
}
