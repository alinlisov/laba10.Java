package lab10.bank;

import java.util.concurrent.locks.ReentrantLock;

/** Adapted bank account from the previous concurrency laboratory. */
public final class BankAccount {
    private final int id;
    private long balance;
    private final ReentrantLock lock = new ReentrantLock();

    public BankAccount(int id, long balance) {
        if (id < 1 || balance < 0) throw new IllegalArgumentException("Invalid account");
        this.id = id;
        this.balance = balance;
    }
    public int id() { return id; }
    public ReentrantLock lock() { return lock; }
    public long balance() {
        lock.lock();
        try { return balance; }
        finally { lock.unlock(); }
    }
    // Call only with BOTH relevant bank-account locks held.
    long balanceLocked() { return balance; }
    void subtractLocked(long amount) { balance -= amount; }
    void addLocked(long amount) { balance = Math.addExact(balance, amount); }
}
