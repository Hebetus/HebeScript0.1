import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Hebescript {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Käyttö: HebeScript [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        }
        else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError) {
            return;
        }



        // Tämä koodilohko on tokenien tulostusta varten

        /**
        for (Token token : tokens) {
            System.out.println(token);
        }
        */
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        }
        else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}

enum TokenType {
    // Yhden merkin lekseemejä vastaavat tokenit

    ASETUS, EROTIN,
    PLUS, MIINUS, KERTAA, JAETTUNA,
    VASEN_SULKU, OIKEA_SULKU, JOS, NIIN, TAI, JA, EI,
    SEURAUS,
    TULOSTASULKU_VASEN, TULOSTASULKU_OIKEA,
    PIENEMPI_KUIN, SUUREMPI_KUIN,
    
    // Kahden merkin lekseemejä (joita on vain yksi) vastaava token

    YHTASUURI_KUIN,

    // Literaalit

    LUKU, MERKKIJONO, TOSI, EPÄTOSI,

    // Avainsanat

    TOISTO, PAATA_TOISTO,
    TULOSTA,
    MUUTTUJA_NIMI,
    
    // Tiedoston päättävä token; vastaa päätä_ohjelma-lekseemiä

    EOF,

    // Nil-token, tyhjiä arvoja varten?

    NIL
}

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

class Scanner {
    private final String source; // Lähdekoodi
    private final List<Token> tokens = new ArrayList<>(); // Tätä täytetään valmiilla tokeneilla, iteroimlla lähdekoodi lekseemeiksi

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // Seuraavan lekseemin alku
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        
        // Yhden merkin lekseemit ja niitä vastaavat tokenit

        switch (c) {
            case '=':
                addToken(match('=') ? TokenType.YHTASUURI_KUIN : TokenType.ASETUS);
                break;

            case ';': addToken(TokenType.EROTIN); break;

            case '^': addToken(TokenType.TOSI); break;
            case '$': addToken(TokenType.EPÄTOSI); break;

            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MIINUS); break;
            case '*': addToken(TokenType.KERTAA); break;
            case '/': addToken(TokenType.JAETTUNA); break;
            
            case '?': addToken(TokenType.JOS); break;
            case '(': addToken(TokenType.VASEN_SULKU); break;
            case ')': addToken(TokenType.OIKEA_SULKU); break;
            case '~': addToken(TokenType.NIIN); break;

            case '|': addToken(TokenType.TAI); break;
            case '&': addToken(TokenType.JA); break;
            case '!': addToken(TokenType.EI); break;

            case '<': addToken(TokenType.PIENEMPI_KUIN); break;
            case '>': addToken(TokenType.SUUREMPI_KUIN); break;

            case '@': addToken(TokenType.TOISTO); break;
            case ':': addToken(TokenType.SEURAUS); break;
            case '#': addToken(TokenType.PAATA_TOISTO); break;

            case '¤': addToken(TokenType.TULOSTA); break;
            case '[': addToken(TokenType.TULOSTASULKU_VASEN); break;
            case ']': addToken(TokenType.TULOSTASULKU_OIKEA); break;

            // Whitespacen ja rivinvaihtojen poisto

            case ' ':
            case '\r':
            case '\t':
                break;
            
            case '\n':
                line++;
                break;
            
            // Yhtä merkkiä pidemmät lekseemit
            
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                }
                else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Hebescript.error(line, "Odottamaton merkki.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        /**
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) {
            type = TokenType.MUUTTUJA_NIMI;
        }
        */
        addToken(TokenType.MUUTTUJA_NIMI);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(TokenType.LUKU, Double.parseDouble(source.substring(start, current)));
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
                advance();
            }
            if (isAtEnd()) {
                Hebescript.error(line, "Päättämätön merkkijono.");
                return;
            }

            advance();
            String value = source.substring(start + 1, current - 1);
            addToken(TokenType.MERKKIJONO, value);
        }
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}

class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    
}