import java.util.Arrays;

public class Memory {
    private byte[] memory;
    private static final int MEMORY_SIZE = 65536; // 64 KB

    public Memory() {
        memory = new byte[MEMORY_SIZE];
        Arrays.fill(memory, (byte) 0);
    }

    // Little-endian formatında 16-bit değer okur
    public int readWord(int address) {
        if (address < 0 || address + 1 >= MEMORY_SIZE) {
            System.err.println("Error: Memory read out of bounds at address " + String.format("0x%04X", address));
            return -1; // veya hata yönetimi
        }
        int lowByte = memory[address] & 0xFF;
        int highByte = memory[address + 1] & 0xFF;
        return (highByte << 8) | lowByte;
    }

    // Little-endian formatında 16-bit değer yazar
    public void writeWord(int address, int value) {
        if (address < 0 || address + 1 >= MEMORY_SIZE) {
            System.err.println("Error: Memory write out of bounds at address " + String.format("0x%04X", address));
            return; // veya hata yönetimi
        }
        memory[address] = (byte) (value & 0xFF);         // Low byte
        memory[address + 1] = (byte) ((value >> 8) & 0xFF); // High byte
    }

    // Programı belleğe yüklemek için (byte byte)
    public void loadProgram(String[] instructions, int loadAddress) {
        int currentAddress = loadAddress;
        for (String instruction : instructions) {
            if (instruction.length() != 16) {
                System.err.println("Error: Invalid instruction format: " + instruction);
                continue;
            }
            try {
                int value = Integer.parseInt(instruction, 2);
                // 16-bit instruction'ı iki byte olarak yaz (little-endian)
                 if (currentAddress + 1 < MEMORY_SIZE) {
                    writeWord(currentAddress, value);
                    currentAddress += 2; // Her komut 16 bit (2 byte)
                 } else {
                     System.err.println("Error: Not enough memory to load instruction at " + String.format("0x%04X", currentAddress));
                     break;
                 }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing instruction: " + instruction);
            }
        }
         System.out.println("Program loaded starting at address: " + String.format("0x%04X", loadAddress));
         System.out.println("Program loaded up to address: " + String.format("0x%04X", currentAddress - 2)); // Son yazılan adres
    }
} 