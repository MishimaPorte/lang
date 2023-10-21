package guru;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.TokenType.*;

class Scanner {
    private String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("please", PLEASE);
        keywords.put("nothing", NOTHING);
        keywords.put("void", VOID);
        keywords.put("or", OR);
        keywords.put("and", AND);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("fun", FUN);
        keywords.put("class", STRUCTURE);
        keywords.put("record", RECORD);
        keywords.put("super", SUPER);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("var", VAR);
        keywords.put("static", STATIC);
        keywords.put("const", CONST);
        keywords.put("with", WITH);
        keywords.put("export", EXPORT);
        keywords.put("while", WHILE);
        keywords.put("from", FROM);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        
        tokens.add(new Token(EOF, "", null, line));

        return tokens;
    }

    boolean isAtEnd() {
        return current >= source.length();
    }

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
            case '?': addToken(QUEST); break;
            case ':': addToken(COLON); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            case '0':case '2':case '4':case '6':case '8':
            case '1':case '3':case '5':case '7':case '9':
                number();
                break;
            default:
                if (isAlpha(c)) identifier(); else {
                    Guru.error(line, "Unexpected token");
                    break;
                }
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType tt) {
        addToken(tt, null);
    }

    private void addToken(TokenType tt, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(tt, text, literal, line));
    }

    private boolean match(char exp) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != exp) return false;

        ++current;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            Guru.error(line, "Unterminated string");
            return;
        }

        advance();

        String val = source.substring(start + 1, current - 1);
        addToken(STRING, val);
    }


    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peek2())) {
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    } 

    private char peek2() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String val = source.substring(start, current);
        TokenType tt = keywords.get(val);
        addToken(tt == null ? IDENTIFIER : tt);
    }
}
