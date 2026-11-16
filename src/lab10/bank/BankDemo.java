package lab10.bank;

import lab10.i18n.Text;
import lab10.logging.AppLogging;
import java.util.List;

/** Exercises FINE, INFO, WARNING, and SEVERE messages on the previous bank task. */
public final class BankDemo {
    private BankDemo() {}
    public static void run(Text text) {
        System.out.println("\n" + text.get("bank.title"));
        BankAccount a = new BankAccount(1, 1000);
        BankAccount b = new BankAccount(2, 500);
        BankAccount c = new BankAccount(3, 200);
        BankService bank = new BankService(List.of(a, b, c), text);
        long before = bank.total();
        System.out.println(text.fmt("bank.before", before));
        bank.transfer(a, b, 250);
        bank.transfer(b, c, 100);
        boolean succeeded = bank.transfer(c, a, 900); // expected rejection
        System.out.println(text.fmt("bank.rejected", succeeded));
        try {
            bank.transfer(a, b, -5); // intentional error for SEVERE logging
        } catch (IllegalArgumentException expected) {
            System.out.println(text.get("bank.invalidHandled"));
        }
        System.out.println(text.fmt("bank.accounts", a.balance(), b.balance(), c.balance()));
        System.out.println(text.fmt("bank.after", bank.total()));
        System.out.println(bank.total() == before ? text.get("bank.conserved") : text.get("bank.invariantFailed"));
        AppLogging.flush();
        System.out.println(text.get("bank.levelExplain"));
        System.out.println(text.fmt("bank.logPath", AppLogging.file().toAbsolutePath()));
    }
}
