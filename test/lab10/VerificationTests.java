package lab10;

import lab10.bank.BankAccount;
import lab10.bank.BankService;
import lab10.i18n.Text;
import lab10.logging.AppLogging;
import lab10.reflection.UnsafeStringEditor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Self-contained integration tests: no JUnit/Maven dependencies needed. */
public final class VerificationTests {
    private static int checks = 0;
    private static final Text UK = Text.ukrainian();
    private VerificationTests() {}

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        System.out.println("PASS: " + name);
        checks++;
    }

    public static void main(String[] args) throws Exception {
        AppLogging.setup();
        try {
            // Reflection: private array changes the SAME String object, not reassignment.
            String original = new String("12345678".toCharArray());
            String alias = original;
            UnsafeStringEditor.replaceContents(original, "ABCDEFGH");
            check("ABCDEFGH".equals(original), "10.1 reflection ASCII replacement");
            check(original == alias, "10.1 same String identity");

            String cyrillic = new String("Привіт".toCharArray());
            UnsafeStringEditor.replaceContents(cyrillic, "Вітаю!");
            check("Вітаю!".equals(cyrillic), "10.1 UTF-16 Cyrillic replacement");
            String unchanged = new String("Cats".toCharArray());
            boolean lengthRejected = false;
            try { UnsafeStringEditor.replaceContents(unchanged, "Too long"); }
            catch (IllegalArgumentException expected) { lengthRejected = true; }
            check(lengthRejected && "Cats".equals(unchanged), "10.1 mismatched length rejected safely");
            check("byte[]".equals(UnsafeStringEditor.storageType()) || "char[]".equals(UnsafeStringEditor.storageType()),
                    "10.1 detects JDK-specific String backing type");

            // Prior lab bank module was extended with a real java.util.logging setup.
            BankAccount a = new BankAccount(1, 200);
            BankAccount b = new BankAccount(2, 300);
            BankService bank = new BankService(List.of(a, b), UK);
            check(bank.total() == 500, "10.2 initial sum");
            check(bank.transfer(a, b, 75), "10.2 successful transfer");
            check(a.balance() == 125 && b.balance() == 375, "10.2 updated balances");
            check(!bank.transfer(a, b, 999), "10.2 insufficient funds rejected");
            check(bank.total() == 500, "10.2 money conservation");
            boolean invalidRejected = false;
            try { bank.transfer(a, b, -1); }
            catch (IllegalArgumentException expected) { invalidRejected = true; }
            check(invalidRejected, "10.2 invalid transfer triggers error");
            boolean duplicateRejected = false;
            try { new BankService(List.of(a, new BankAccount(1, 10)), UK); }
            catch (IllegalArgumentException expected) { duplicateRejected = true; }
            check(duplicateRejected, "10.2 duplicate account ID rejected");

            List<BankAccount> many = new ArrayList<>();
            for (int i = 1; i <= 20; i++) many.add(new BankAccount(i, 1000));
            BankService concurrentBank = new BankService(many, UK);
            AppLogging.get().setLevel(Level.WARNING); // avoid flooding the console during stress test
            ExecutorService executor = Executors.newFixedThreadPool(12);
            List<Future<?>> futures = new ArrayList<>();
            try {
                for (int i = 0; i < 400; i++) {
                    final int k = i;
                    futures.add(executor.submit(() -> {
                        BankAccount from = many.get(k % many.size());
                        BankAccount to = many.get((k + 1) % many.size());
                        concurrentBank.transfer(from, to, 1);
                    }));
                }
                for (Future<?> future : futures) future.get(10, TimeUnit.SECONDS);
            } finally { executor.shutdownNow(); AppLogging.get().setLevel(Level.FINE); }
            check(concurrentBank.total() == 20_000, "10.2 concurrent bank preserves total");
            check(many.stream().allMatch(x -> x.balance() >= 0), "10.2 no negative balances");

            Logger log = AppLogging.get();
            check(log.getLevel().equals(Level.FINE), "10.2 logger threshold FINE");
            boolean consoleInfo = false, fileFine = false;
            for (Handler h : log.getHandlers()) {
                if (h instanceof java.util.logging.ConsoleHandler && h.getLevel() == Level.INFO) consoleInfo = true;
                if (h instanceof java.util.logging.FileHandler && h.getLevel() == Level.FINE) fileFine = true;
            }
            check(consoleInfo, "10.2 console filters out FINE");
            check(fileFine, "10.2 file receives FINE and higher levels");
            log.fine("SPECIAL_FINE_ONLY_IN_FILE_1001");
            log.info("SPECIAL_INFO_1002");
            AppLogging.flush();
            String content = Files.readString(AppLogging.file(), StandardCharsets.UTF_8);
            check(content.contains("SPECIAL_FINE_ONLY_IN_FILE_1001"), "10.2 FINE written to .log");
            check(content.contains("SPECIAL_INFO_1002"), "10.2 INFO written to .log");
            check(content.contains("WARNING") && content.contains("SEVERE"), "10.2 errors recorded in file");

            check(Text.ukrainian().get("menu.language").contains("Вибір мови"), "10.3 Ukrainian bundle");
            check(Text.english().get("menu.language").contains("Choose language"), "10.3 English bundle");
            check(Text.polish().get("menu.language").contains("Wybór języka"), "10.3 Polish bundle");
            check(Text.english().fmt("bank.before", "1700").contains("1700"), "10.3 MessageFormat substitution");
            check(!Text.ukrainian().get("menu.title").equals(Text.english().get("menu.title")),
                    "10.3 changing locale changes UI text");
            System.out.println("\nУСПІШНО: " + checks + " із " + checks + " перевірок.");
        } finally { AppLogging.shutdown(); }
    }
}
