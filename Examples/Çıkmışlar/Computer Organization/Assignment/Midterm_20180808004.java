import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Midterm_20180808004 {
    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<String> commands = getDataFromFilePath(args[0]);
        boolean isStart = false;
        int AC = 0;
        int[] M = new int[256];
        int F = 0;
        for (int PC = 0; PC < commands.size(); PC++) {
            String baseCommand = commands.get(PC);
            int commandNum = 0;
            if (commands.get(PC).contains(" ")) {
                baseCommand = commands.get(PC).substring(0, commands.get(PC).indexOf(" "));
                commandNum = Integer.parseInt(commands.get(PC).substring(commands.get(PC).indexOf(" ") + 1));
            }
            if (isStart || baseCommand.equals("START"))
                switch (baseCommand) {
                    case "START":
                        isStart = true;
                        break;
                    case "LOAD":
                        AC = commandNum;
                        break;
                    case "LOADM":
                        AC = M[commandNum];
                        break;
                    case "STORE":
                        M[commandNum] = AC;
                        break;
                    case "CMPM":
                        if (AC > M[commandNum]) F = 1;
                        else if (AC < M[commandNum]) F = -1;
                        else F = 0;
                        break;
                    case "CJMP":
                        if (F > 0) PC = commandNum - 1;
                        break;
                    case "JMP":
                        PC = commandNum - 1;
                        break;
                    case "ADD":
                        AC = AC + commandNum;
                        break;
                    case "ADDM":
                        AC = AC + M[commandNum];
                        break;
                    case "SUBM":
                        AC -= M[commandNum];
                        break;
                    case "SUB":
                        AC -= commandNum;
                        break;
                    case "MUL":
                        AC *= commandNum;
                        break;
                    case "MULM":
                        AC *= M[commandNum];
                        break;
                    case "DISP":
                        System.out.println(AC);
                        break;
                    case "HALT":
                        isStart = false;
                        break;
                }
        }
    }

    private static ArrayList<String> getDataFromFilePath(String path) throws FileNotFoundException {
        ArrayList<String> commands = new ArrayList<>();
        File file = new File(path);
        Scanner siteler = new Scanner(file);
        while (siteler.hasNextLine()) {
            String str = siteler.nextLine().toUpperCase().trim();
            if (!str.contains("%")) {
                while (str.contains("  ")) str = str.replace("  ", " ");
                str = str.substring(str.indexOf(" ") + 1);
                if (!str.equals("")) commands.add(str);
            }
        }
        return commands;
    }
}
