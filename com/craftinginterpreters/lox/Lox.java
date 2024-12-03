package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    private final String source;  // �\�[�X�R�[�h
    private final List<Token> tokens = new ArrayList<>();  // �g�[�N����ێ����郊�X�g
    private int start = 0;  // ���ݏ������̕�����̊J�n�ʒu
    private int current = 0;  // ���ݏ������̕����̈ʒu
    private int line = 1;  // ���݂̍s�ԍ�

    // �L�[���[�h�̃}�b�s���O
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

    // �R���X�g���N�^
    Scanner(String source) {
        this.source = source;
    }

    // �g�[�N�����X�L�������ă��X�g��Ԃ�
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));  // �\�[�X�R�[�h�̏I���������g�[�N����ǉ�
        return tokens;
    }

    // �g�[�N����1�X�L��������
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
                    // �R�����g�͍s�̏I���܂ő���
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
                    addToken(OR);  // 'or' �����ʎq�Ƃ��ď���
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                // �󔒂͖���
                break;

            case '\n':
                line++;  // �V�����s���n�܂邲�Ƃɍs�ԍ����X�V
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

    // �\�[�X�̏I��肩�ǂ������`�F�b�N
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // ������1�i�߂�
    private char advance() {
        return source.charAt(current++);
    }

    // ���̕�������v�����true��Ԃ�
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    // ���̕������`�F�b�N����
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // �g�[�N����ǉ�����
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // ���������������
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // �I���� " ��i�߂�
        advance();

        // �ۊ��ʂň͂܂ꂽ�������؂���
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    // ��������������
    private void number() {
        while (isDigit(peek())) advance();

        // �����_�̕�����T��
        if (peek() == '.' && isDigit(peekNext())) {
            // �����_��i�߂�
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // ���̕������`�F�b�N�i�����_����ɗ���ꍇ�̂��߁j
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // �������ǂ����𔻒肷��
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // ���ʎq����������
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // �L�[���[�h���ǂ����𒲂ׁA�L�[���[�h�łȂ����IDENTIFIER
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    // �A���t�@�x�b�g���A���_�[�X�R�A�𔻒�
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    // �A���t�@�x�b�g���������𔻒�
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
