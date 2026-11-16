package lab10.bank;

import lab10.i18n.Text;
import lab10.logging.AppLogging;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/** Thread-safe bank transfers reused from Laboratory No. 9, enhanced with JUL logging. */
public final class BankService {
    private final List<BankAccount> accounts;
    private final Set<BankAccount> members;
    private final Text text;
    private final Logger log = AppLogging.get();

    public BankService(List<BankAccount> accounts, Text text) {
        if (accounts == null || accounts.isEmpty()) throw new IllegalArgumentException("Accounts missing");
        this.accounts = List.copyOf(accounts);
        this.members = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<Integer> ids = new HashSet<>();
        for (BankAccount account : accounts) {
            if (account == null || !ids.add(account.id()) || !members.add(account)) {
                throw new IllegalArgumentException("Account IDs and objects must be unique");
            }
        }
        this.text = text;
    }

    public boolean transfer(BankAccount from, BankAccount to, long amount) {
        if (from == null || to == null || !members.contains(from) || !members.contains(to)
                || amount <= 0 || from == to) {
            log.severe(text.get("log.invalid"));
            throw new IllegalArgumentException("Invalid transfer");
        }
        log.fine(text.fmt("log.request", from.id(), to.id(), amount));
        BankAccount first = from.id() < to.id() ? from : to;
        BankAccount second = from.id() < to.id() ? to : from;
        first.lock().lock();
        try {
            second.lock().lock();
            try {
                if (from.balanceLocked() < amount) {
                    log.warning(text.fmt("log.insufficient", from.id(), amount));
                    return false;
                }
                // Check overflow before subtracting to maintain atomicity on failure.
                Math.addExact(to.balanceLocked(), amount);
                from.subtractLocked(amount);
                to.addLocked(amount);
                log.info(text.fmt("log.success", from.id(), to.id(), amount));
                return true;
            } finally { second.lock().unlock(); }
        } finally { first.lock().unlock(); }
    }

    /** Use after all ongoing transfers have finished. */
    public long total() {
        long result = 0;
        for (BankAccount account : accounts) result = Math.addExact(result, account.balance());
        return result;
    }
}
