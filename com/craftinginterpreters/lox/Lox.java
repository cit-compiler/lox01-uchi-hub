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
            System.out.println("�g�p���@: jlox [script]");
            System.exit(64); // �s���Ȉ����G���[�R�[�h
        } else if (args.length == 1) {
            runFile(args[0]); // �X�N���v�g�t�@�C�������s
        } else {
            runPrompt(); // �Θb���[�h�����s
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
        // �����ɃX�N���v�g�̎��s���W�b�N������
        System.out.println("���s: " + source);
    }
}
