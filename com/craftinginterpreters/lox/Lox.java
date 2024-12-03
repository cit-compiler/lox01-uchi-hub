package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("使用方法: jlox [script]");
            System.exit(64); // 不正な引数エラーコード
        } else if (args.length == 1) {
            runFile(args[0]); // スクリプトファイルを実行
        } else {
            runPrompt(); // 対話モードを実行
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String script = new String(bytes);
        run(script);
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null || line.equalsIgnoreCase("exit")) break;
            run(line);
        }
    }

    private static void run(String source) {
        // ここにスクリプトの実行ロジックを実装
        System.out.println("実行: " + source);
    }
}
