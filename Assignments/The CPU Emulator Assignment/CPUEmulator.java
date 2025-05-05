import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CPUEmulator {
    private Memory memory;
    private Cache cache;
    private int pc; // Program Counter (absolute address)
    private int ac; // Accumulator
    private int compareFlag; // 0: equal, 1: greater, -1: less
    private boolean halt = false;
    private int loadAddress;

    public CPUEmulator(String programFile, String configFile) throws IOException {
        memory = new Memory();
        cache = new Cache(memory);

        // Config dosyasını oku
        List<String> configLines = Files.readAllLines(Paths.get(configFile));
        if (configLines.size() < 2) {
            throw new IOException("Config file is missing load address or initial PC.");
        }
        loadAddress = Integer.parseInt(configLines.get(0).substring(2), 16);
        pc = Integer.parseInt(configLines.get(1).substring(2), 16); // Initial PC is absolute

        // Program dosyasını oku ve belleğe yükle
        List<String> programLines = Files.readAllLines(Paths.get(programFile));
        memory.loadProgram(programLines.toArray(new String[0]), loadAddress);

        // Başlangıç değerleri
        ac = 0;
        compareFlag = 0;
        halt = false;
    }

    public void run() {
        System.out.println("Starting emulation. Initial PC: " + String.format("0x%04X", pc));
        while (!halt) {
            int instructionAddress = pc;
            // Fetch instruction (word) from memory via cache
            int instruction = cache.readWord(pc);
            if (instruction == -1) { // Bellek okuma hatası veya geçersiz adres
                 System.err.println("Emulator HALT: Failed to fetch instruction at PC = " + String.format("0x%04X", pc));
                 halt = true;
                 break;
            }

            pc += 2; // PC'yi bir sonraki komut için ilerlet (her komut 2 byte)

            // Decode instruction
            int opcode = (instruction >> 12) & 0xF; // İlk 4 bit
            int operand = instruction & 0xFFF;      // Son 12 bit (adres veya anlık değer)

            // Execute instruction
            // System.out.println(String.format("PC: 0x%04X, IR: 0x%04X, Op: %d, Opd: 0x%03X, AC: %d, Flag: %d", instructionAddress, instruction, opcode, operand, ac, compareFlag));
            execute(opcode, operand);
        }

        System.out.println("Emulation finished.");
    }

    private void execute(int opcode, int operand) {
        int address; // Bellek operasyonları için göreceli adresin hesaplanmış hali
        int alignedAddress; // Kelime sınırına hizalanmış adres
        int value;

        switch (opcode) {
            case 0b0000: // START
                // Genellikle bir işlem yapmaz, sadece programın başlangıç noktasıdır.
                break;
            case 0b0001: // LOAD Immediate
                ac = operand;
                break;
            case 0b0010: // LOADM Memory (cached)
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for LOADM");
                }
                ac = cache.readWord(alignedAddress);
                break;
            case 0b0011: // STORE Memory (cached)
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                 if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for STOREM");
                }
                cache.writeWord(alignedAddress, ac);
                break;
            case 0b0100: // CMPM Memory
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for CMPM");
                }
                value = cache.readWord(alignedAddress);
                if (ac > value) {
                    compareFlag = 1;
                } else if (ac < value) {
                    compareFlag = -1;
                } else {
                    compareFlag = 0;
                }
                break;
            case 0b0101: // CJMP (Conditional Jump)
                if (compareFlag > 0) {
                    pc = loadAddress + operand; // Şartlı atlama (program içindeki göreceli adrese)
                }
                break;
            case 0b0110: // JMP (Unconditional Jump)
                pc = loadAddress + operand; // Koşulsuz atlama (program içindeki göreceli adrese)
                break;
            case 0b0111: // ADD Immediate
                ac += operand;
                 ac &= 0xFFFF; // 16 bit sınırı için taşmayı önle (opsiyonel, tanımda belirtilmemişse)
                break;
            case 0b1000: // ADDM Memory (cached)
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                 if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for ADDM");
                }
                value = cache.readWord(alignedAddress);
                ac += value;
                 ac &= 0xFFFF; // 16 bit sınırı için
                break;
            case 0b1001: // SUB Immediate
                ac -= operand;
                 ac &= 0xFFFF; // 16 bit sınırı için
                break;
            case 0b1010: // SUBM Memory (cached)
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                 if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for SUBM");
                }
                value = cache.readWord(alignedAddress);
                ac -= value;
                 ac &= 0xFFFF; // 16 bit sınırı için
                break;
            case 0b1011: // MUL Immediate
                ac *= operand;
                 ac &= 0xFFFF; // 16 bit sınırı için
                break;
            case 0b1100: // MULM Memory (cached)
                address = loadAddress + operand; // Göreceli adres hesapla
                alignedAddress = address & ~1; // Adresi aşağıya doğru en yakın çift sayıya hizala
                 if (address != alignedAddress) {
                     // System.out.println("Warning: Address " + String.format("0x%04X", address) + " aligned down to " + String.format("0x%04X", alignedAddress) + " for MULM");
                }
                value = cache.readWord(alignedAddress);
                ac *= value;
                 ac &= 0xFFFF; // 16 bit sınırı için
                break;
            case 0b1101: // DISP
                System.out.println("Value in AC: " + ac);
                break;
            case 0b1110: // HALT
                halt = true;
                break;
            default:
                System.err.println("Error: Unknown opcode: " + String.format("0x%X", opcode) + " at address " + String.format("0x%04X", pc - 2) );
                halt = true;
                break;
        }
         // 16-bit taşma kontrolü (negatif sayılar için de çalışır)
        if (ac > 32767) { // 2^15 - 1
            ac = ac - 65536; // Wrap around (örneğin, 32768 -> -32768)
        } else if (ac < -32768) { // -2^15
            ac = ac + 65536; // Wrap around
        }
    }

    public int getAccumulatorValue() {
        return ac;
    }

    public double getCacheHitRatio() {
        return cache.getHitRatio();
    }
} 