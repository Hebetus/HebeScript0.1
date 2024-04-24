import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return asetuslause();
        }
        catch (ParseError error) {
            return null;
        }
    }

    // Jokaista tulosääntöä varten oma metodi
    
    // KESKEN (MAHDOLLISESTI TARPEETON)

    private Expr kaannosyksikko() {
        if (match(TokenType.JOS)) {
            Expr expr = evaluaatio();
            return expr;
        }

        return null;
    }

    // KESKEN

    private Expr asetuslause() {
        if (match(TokenType.ASETUS)) {
            return new Expr.Binary(null, null, null);
        }
        return null;
    }
    
    // KESKEN

    private Expr muuttujanarvo() {
        return null;
    }

    // KESKEN

    private Expr evaluaatio() {
        Expr expr = evaluaatiolauseke();
        return expr;
    }

    // KESKEN

    private Expr evaluaatiolauseke() {
        Expr expr = null;

        return expr;
    }

    // KESKEN

    private Expr toistosilmukka() {
        return null;
    }

    // KESKEN

    private Expr tulostus() {
        return null;
    }

    // Metodi binäärilausekeolioiden muodostamiseen
    
    private Expr binaarilauseke() {
        Expr left = new Expr.Literal(previous().literal);
        Expr right = new Expr.Literal(next().literal);

        if (match(TokenType.SUUREMPI_KUIN)) {
            return new Expr.Binary(left, peek(), right);
        }
        if (match(TokenType.PIENEMPI_KUIN)) {
            return new Expr.Binary(left, peek(), right);
        }
        if (match(TokenType.YHTASUURI_KUIN)) {
            return new Expr.Binary(left, peek(), right);
        }

        if (match(TokenType.PLUS)) {
            return new Expr.Binary(left, peek(), right);
        }
        if (match(TokenType.MIINUS)) {
            return new Expr.Binary(left, peek(), right);
        }
        if (match(TokenType.KERTAA)) {
            return new Expr.Binary(left, peek(), right);
        }
        if (match(TokenType.JAETTUNA)) {
            return new Expr.Binary(left, peek(), right);
        }
        else {
            return null;
        }
    }

    // Metodi literaalilausekeolioiden muodostukseen

    private Expr literaali() {
        if (match(TokenType.EPÄTOSI)) {
            return new Expr.Literal(false);
        }
        if (match(TokenType.TOSI)) {
            return new Expr.Literal(true);
        }
        if (match(TokenType.NIL)) {
            return new Expr.Literal(null);
        }

        if (match(TokenType.LUKU, TokenType.MERKKIJONO)) {
            return new Expr.Literal(previous().literal);
        }
        else {
            return null;
        }
    }

    // Parsimisen apumetodit

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token next() {
        return tokens.get(current + 1);
    }

    private ParseError error(Token token, String message) {
        Hebescript.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.EROTIN) {
                return;
            }

            switch (peek().type) {
                case MUUTTUJA_NIMI:
                case JOS:
                case TOISTO:
                case TULOSTA:
                    return;
            }
        }
    }
}