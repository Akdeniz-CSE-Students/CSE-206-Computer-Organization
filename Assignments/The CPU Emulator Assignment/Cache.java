import java.util.Arrays;

public class Cache {

    private static final int CACHE_SIZE = 16; // Toplam byte
    private static final int BLOCK_SIZE = 2;  // Byte cinsinden blok boyutu (16 bit = 2 byte)
    private static final int NUM_BLOCKS = CACHE_SIZE / BLOCK_SIZE; // 16 / 2 = 8 blok

    private class CacheBlock {
        boolean valid = false;
        int tag = -1;
        byte[] data = new byte[BLOCK_SIZE]; // Her blok 2 byte veri tutar
    }

    private CacheBlock[] cacheBlocks = new CacheBlock[NUM_BLOCKS];
    private Memory memory;
    private int hits = 0;
    private int misses = 0;

    public Cache(Memory memory) {
        this.memory = memory;
        for (int i = 0; i < NUM_BLOCKS; i++) {
            cacheBlocks[i] = new CacheBlock();
        }
    }

    // Adresten 16-bit (word) oku
    public int readWord(int address) {
        if (address % 2 != 0) {
            System.err.println("Cache Error: Address must be word-aligned (even) for readWord: " + String.format("0x%04X", address));
            // Hata yönetimi veya varsayılan değer döndürme
            return -1; // veya uygun bir hata kodu
        }

        int blockIndex = (address / BLOCK_SIZE) % NUM_BLOCKS; // Direct mapping
        int tag = address / (NUM_BLOCKS * BLOCK_SIZE); // Adresin üst bitleri

        CacheBlock block = cacheBlocks[blockIndex];

        if (block.valid && block.tag == tag) {
            // Cache Hit
            hits++;
            // Little-endian okuma
            int lowByte = block.data[0] & 0xFF;
            int highByte = block.data[1] & 0xFF;
             // System.out.println("Cache Hit (Read) - Address: " + String.format("0x%04X", address) + " Index: " + blockIndex + " Tag: " + tag + " Data: " + String.format("0x%04X", (highByte << 8) | lowByte));
            return (highByte << 8) | lowByte;
        } else {
            // Cache Miss
            misses++;
            // Bellekten bloğu getir (sadece istenen word'ü değil, tüm bloğu)
            int blockStartAddress = (address / BLOCK_SIZE) * BLOCK_SIZE;
            // System.out.println("Cache Miss (Read) - Address: " + String.format("0x%04X", address) + " Index: " + blockIndex + " Tag: " + tag + " Fetching block: " + String.format("0x%04X", blockStartAddress));

            // Bellekten 2 byte (1 word) oku ve cache bloğuna yerleştir
            int valueFromMemory = memory.readWord(blockStartAddress);
            if (valueFromMemory == -1) {
                 System.err.println("Cache Error: Failed to read from memory during cache miss handling for address: " + String.format("0x%04X", address));
                 return -1; // Bellek okuma hatası
            }

            block.valid = true;
            block.tag = tag;
            // Little-endian yazma
            block.data[0] = (byte) (valueFromMemory & 0xFF);
            block.data[1] = (byte) ((valueFromMemory >> 8) & 0xFF);

            return valueFromMemory;
        }
    }

    // Adrese 16-bit (word) yaz (Write-Through)
    public void writeWord(int address, int value) {
        if (address % 2 != 0) {
             System.err.println("Cache Error: Address must be word-aligned (even) for writeWord: " + String.format("0x%04X", address));
            // Hata yönetimi
            return;
        }

        // Önce belleğe yaz (Write-Through)
        memory.writeWord(address, value);
        // System.out.println("Cache Write (to Memory) - Address: " + String.format("0x%04X", address) + " Value: " + String.format("0x%04X", value));

        int blockIndex = (address / BLOCK_SIZE) % NUM_BLOCKS;
        int tag = address / (NUM_BLOCKS * BLOCK_SIZE);

        CacheBlock block = cacheBlocks[blockIndex];

        // Eğer ilgili blok cache'de ise, onu da güncelle
        if (block.valid && block.tag == tag) {
            hits++; // Write-through'da yazma işlemi hit sayılır mı? Genellikle evet.
            // Little-endian yazma
            block.data[0] = (byte) (value & 0xFF);
            block.data[1] = (byte) ((value >> 8) & 0xFF);
            // System.out.println("Cache Hit (Write) - Address: " + String.format("0x%04X", address) + " Index: " + blockIndex + " Tag: " + tag + " Updated Data: " + String.format("0x%04X", value));
        } else {
             misses++; // Yazma işleminde miss oldu, bellek güncellendi, cache'e dokunulmadı (write-no-allocate olabilir veya allocate edilebilir, burada allocate etmiyoruz)
             // System.out.println("Cache Miss (Write) - Address: " + String.format("0x%04X", address) + " Index: " + blockIndex + " Tag: " + tag + " Memory updated, cache block not allocated/updated.");
        }
    }

    public double getHitRatio() {
        int totalAccesses = hits + misses;
        if (totalAccesses == 0) {
            return 0.0;
        }
        return (double) hits / totalAccesses * 100.0;
    }

    public int getHits() {
        return hits;
    }

    public int getMisses() {
        return misses;
    }
} 