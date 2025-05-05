import java.io.IOException;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // Ondalık ayırıcı olarak nokta kullanılmasını sağlar

        if (args.length < 2) {
            System.err.println("Usage: java Main <program_file> <config_file>");
            System.exit(1);
        }

        String programFile = args[0];
        String configFile = args[1];

        try {
            CPUEmulator emulator = new CPUEmulator(programFile, configFile);
            emulator.run();

            // Sonuçları yazdır
            // System.out.println("Final Accumulator Value: " + emulator.getAccumulatorValue()); // DISP komutu zaten yazdırıyor
            System.out.printf("Cache hit ratio: %.2f%%\n", emulator.getCacheHitRatio());

        } catch (IOException e) {
            System.err.println("Error loading files: " + e.getMessage());
            System.exit(1);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number in config file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 