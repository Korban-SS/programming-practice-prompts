import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class PPPLogFileErrorAnalyzer {
    private static final Map<Integer, String> logValue = new HashMap<>();
    private static LocalDateTime currentDateTime = LocalDateTime.now();
    private static final StringBuilder fileData = new StringBuilder();
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    // file-level
    private static final int maxLines = 5000;
    private static double emptyFileChance = 0.08;
    private static double noErrorEntryChance = 0.1;

    // data-level
    private static double duplicateLogChance = 0.25;
    private static double invalidTimestampChance = 0.12;
    private static double missingFieldChance = 0.1;

    public static void main(String[] args) {
        loadLogValues();

        // Empty file edge case
        if (makeHappen(emptyFileChance)) {
            saveFile("");
            return;
        }

        boolean noError = makeHappen(noErrorEntryChance);

        int logLines = random.nextInt(maxLines - 1) + 1; // avoid 0 lines

        fileData.append(formatDateTime()).append(",INFO,System started\n");

        for (int i = 0; i < logLines; i++) {

            int logEntryKey = getValidLogKey(noError);

            String timestamp = makeHappen(invalidTimestampChance)
                    ? formatCorruptDateTime()
                    : formatDateTime();

            String[] logParts = logValue.get(logEntryKey).split(",", 2);

            List<String> fields = new ArrayList<>();
            fields.add(timestamp);
            fields.add(logParts[0]); // level
            fields.add(logParts[1]); // message

            // Missing field edge case
            if (makeHappen(missingFieldChance) && fields.size() > 1) {
                int removeIndex = random.nextInt(fields.size());
                fields.remove(removeIndex);
            }

            String line = String.join(",", fields);

            // Duplicate edge case
            if (makeHappen(duplicateLogChance)) {
                fileData.append(line).append('\n');
            }

            fileData.append(line).append('\n');

            // Advance time safely
            currentDateTime = currentDateTime.plusSeconds(random.nextInt(5) + 1);
        }

        saveFile(fileData.toString());
        System.out.println("Created system_log.txt");
    }

    private static void loadLogValues() {
        logValue.put(1, "ERROR,Database connection failed");
        logValue.put(2, "ERROR,Null pointer exception");
        logValue.put(3, "ERROR,File not found");
        logValue.put(4, "ERROR,Service unavailable");
        logValue.put(5, "ERROR,Timeout occurred");
        logValue.put(6, "ERROR,Failed to parse response");
        logValue.put(7, "ERROR,Access denied");
        logValue.put(8, "ERROR,Out of memory");
        logValue.put(9, "ERROR,Invalid input format");

        logValue.put(10, "WARNING,High CPU usage detected");
        logValue.put(11, "WARNING,Deprecated API usage");
        logValue.put(12, "WARNING,Disk space running low");
        logValue.put(13, "WARNING,Low memory");
        logValue.put(14, "WARNING,Network latency detected");
        logValue.put(15, "WARNING,Configuration mismatch");

        logValue.put(16, "INFO,User Login");
        logValue.put(17, "INFO,Configuration loaded");
        logValue.put(18, "INFO,File uploaded successfully");
        logValue.put(19, "INFO,User Logout");

        logValue.put(20, "DEBUG,Connection parameters set");
        logValue.put(21, "DEBUG,Retrying connection");
        logValue.put(22, "DEBUG,Initializing module");
        logValue.put(23, "DEBUG,Cache cleared");
    }

    private static int getValidLogKey(boolean noError) {
        while (true) {
            int key = random.nextInt(logValue.size()) + 1;
            if (!noError) return key;

            String level = logValue.get(key).split(",")[0];
            if (!level.equals("ERROR")) return key;
        }
    }

    private static boolean makeHappen(double chance) {
        return random.nextDouble() < chance;
    }

    private static String formatDateTime() {
        return currentDateTime.toLocalDate() + " " + currentDateTime.toLocalTime().withNano(0);
    }

    private static String formatCorruptDateTime() {
        String normal = formatDateTime();

        switch (random.nextInt(4)) {
            case 0:
                return "??" + normal.replace(":", "") + "**";
            case 1:
                return normal.replace("-", "/").replace(":", "::");
            case 2:
                return "INVALID_TIMESTAMP";
            case 3:
                return new StringBuilder(normal).reverse().toString();
            default:
                return normal;
        }
    }

    private static void saveFile(String data) {
        try (FileWriter fileWriter = new FileWriter("system_log.txt")) {
            fileWriter.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}