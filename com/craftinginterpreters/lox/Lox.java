package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    private final String source;  // ソースコード
    private final List<Token> tokens = new ArrayList<>();  // トークンを保持するリスト
    private int start = 0;  // 現在処理中の文字列の開始位置
    private int current = 0;  // 現在処理中の文字の位置
    private int line = 1;  // 現在の行番号

    // キーワードのマッピング
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    // コンストラクタ
    Scanner(String source) {
        this.source = source;
    }

    // トークンをスキャンしてリストを返す
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));  // ソースコードの終わりを示すトークンを追加
        return tokens;
    }

    // トークンを1つスキャンする
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '/':
                if (match('/')) {
                    // コメントは行の終わりまで続く
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case '"': string(); break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            case 'o':
                if (match('r')) {
                    addToken(OR);  // 'or' を識別子として処理
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // 空白は無視
                break;

            case '\n':
                line++;  // 新しい行が始まるごとに行番号を更新
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    // ソースの終わりかどうかをチェック
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // 文字を1つ進める
    private char advance() {
        return source.charAt(current++);
    }

    // 次の文字が一致すればtrueを返す
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // 次の文字をチェックする
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // トークンを追加する
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // 文字列を処理する
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // 終了の " を進める
        advance();

        // 丸括弧で囲まれた文字列を切り取る
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // 数字を処理する
    private void number() {
        while (isDigit(peek())) advance();

        // 小数点の部分を探す
        if (peek() == '.' && isDigit(peekNext())) {
            // 小数点を進める
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // 次の文字をチェック（小数点が後に来る場合のため）
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // 数字かどうかを判定する
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // 識別子を処理する
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // キーワードかどうかを調べ、キーワードでなければIDENTIFIER
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // アルファベットかアンダースコアを判定
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    // アルファベットか数字かを判定
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
