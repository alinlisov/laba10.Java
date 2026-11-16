package lab10;

import lab10.bank.BankDemo;
import lab10.i18n.Text;
import lab10.logging.AppLogging;
import lab10.reflection.ReflectionDemo;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

/** The multilingual console menu for laboratory 10. */
public final class Main {
    private Main() {}

    public static void main(String[] args) {
        Text text = Text.ukrainian();
        try {
            AppLogging.setup();
        } catch (IOException ex) {
            System.err.println("Could not initialize logging: " + ex.getMessage());
            return;
        }
        try (Scanner input = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.println("\n" + text.get("menu.title"));
                System.out.println(text.get("menu.reflection"));
                System.out.println(text.get("menu.bank"));
                System.out.println(text.get("menu.language"));
                System.out.println(text.get("menu.logs"));
                System.out.println(text.get("menu.exit"));
                System.out.print(text.get("menu.choice"));
                if (!input.hasNextLine()) break;
                String selected = input.nextLine().trim();
                switch (selected) {
                    case "1" -> ReflectionDemo.run(input, text);
                    case "2" -> BankDemo.run(text);
                    case "3" -> text = chooseLanguage(input, text);
                    case "4" -> printLog(text);
                    case "0" -> { System.out.println(text.get("menu.goodbye")); return; }
                    default -> System.out.println(text.get("menu.invalid"));
                }
            }
        } finally { AppLogging.shutdown(); }
    }

    private static Text chooseLanguage(Scanner input, Text previous) {
        System.out.println(previous.get("language.prompt"));
        System.out.println("1 - Українська | 2 - English | 3 - Polski");
        System.out.print(previous.get("menu.choice"));
        if (!input.hasNextLine()) return previous;
        Text changed = switch (input.nextLine().trim()) {
            case "1" -> Text.ukrainian();
            case "2" -> Text.english();
            case "3" -> Text.polish();
            default -> previous;
        };
        System.out.println(changed.get("language.selected"));
        return changed;
    }

    private static void printLog(Text text) {
        AppLogging.flush();
        try {
            System.out.println("\n" + text.get("logs.title"));
            System.out.println(Files.readString(AppLogging.file(), StandardCharsets.UTF_8));
        } catch (IOException ex) {
            System.out.println(text.fmt("logs.error", ex.getMessage()));
        }
    }
}
