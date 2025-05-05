# CSE206 Computer Organization - Assignment 1: CPU Emulator

## Student Information

*   **Name:** Yahya Efe Kuruçay
*   **Student ID:** 20220808005

## Project Description

This project implements a simple CPU emulator in Java, simulating a 16-bit processor with a basic instruction set, 64KB of main memory, and a direct-mapped cache.

The emulator reads a program consisting of 16-bit binary instructions from a file (`program.txt`) and configuration settings (load address, initial PC) from another file (`config.txt`). It then executes the program, simulating memory accesses through a write-through cache and tracking cache hit/miss statistics.

## Components

*   `Main.java`: Entry point of the application. Parses arguments and starts the emulator.
*   `CPUEmulator.java`: Core class containing the CPU logic (registers, fetch-decode-execute cycle, instruction implementation).
*   `Memory.java`: Simulates the 64KB byte-addressable main memory.
*   `Cache.java`: Implements the direct-mapped, 16-byte, write-through cache logic.
*   `program.txt`: Contains the binary program instructions.
*   `config.txt`: Contains the load address and initial PC value.

## Java Version

This project was developed and tested using **Java 17**. Compatibility with other versions is not guaranteed.

## How to Compile and Run

1.  **Compile:**
    Open a terminal or command prompt in the project's root directory (where the `.java` files are located) and run:
    ```bash
    javac Main.java CPUEmulator.java Cache.java Memory.java
    ```
    This will generate the corresponding `.class` files.

2.  **Run:**
    Execute the compiled program using the following command, providing the program and config files as arguments:
    ```bash
    java Main program.txt config.txt
    ```

## Expected Output (for the provided sample program)

The emulator should print the final value of the Accumulator (AC) when the `DISP` instruction is executed, followed by the cache hit ratio:

```
Value in AC: 210
Cache hit ratio: 42.86%
```

*(Note: Debug prints within the code might produce additional output if uncommented.)* 