package lab10.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/** Logger level FINE; console threshold INFO; text-file threshold FINE. */
public final class AppLogging {
    private static final Logger LOGGER = Logger.getLogger("lab10.bank");
    private static final Path PATH = Path.of("logs", "lab10.log");

    private AppLogging() {}
    public static Logger get() { return LOGGER; }
    public static Path file() { return PATH; }

    public static void setup() throws IOException {
        Files.createDirectories(PATH.getParent());
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.FINE);
        for (Handler old : LOGGER.getHandlers()) {
            LOGGER.removeHandler(old);
            old.close();
        }
        Formatter formatter = new Formatter() {
            private final DateTimeFormatter df = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
            @Override public String format(LogRecord record) {
                return df.format(LocalDateTime.now()) + " | " + record.getLevel().getName()
                        + " | " + formatMessage(record) + System.lineSeparator();
            }
        };
        ConsoleHandler console = new ConsoleHandler();
        console.setEncoding(StandardCharsets.UTF_8.name());
        console.setFormatter(formatter);
        console.setLevel(Level.INFO);
        FileHandler file = new FileHandler(PATH.toString(), true);
        file.setEncoding(StandardCharsets.UTF_8.name());
        file.setFormatter(formatter);
        file.setLevel(Level.FINE);
        LOGGER.addHandler(console);
        LOGGER.addHandler(file);
    }

    public static void flush() {
        for (Handler handler : LOGGER.getHandlers()) handler.flush();
    }

    public static void shutdown() {
        for (Handler handler : LOGGER.getHandlers()) {
            handler.flush();
            handler.close();
            LOGGER.removeHandler(handler);
        }
    }
}
