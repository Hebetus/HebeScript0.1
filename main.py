import re

def identifier(scanner, token): return "IDENT", token

def end_stmt(scanner, token): return "END_STATEMENT", token

def number(scanner, token): return "NUMBER", token
def digit(scanner, token): return "DIGIT", token
def character(scanner, token): return "CHARACTER", token

def operator(scanner, token): return "OPERATOR", token

scanner = re.Scanner([
    (r"[a-zA-Z_]\w*", identifier),

    (r"\;", end_stmt),

    (r"[0-9]", number),
    (r"[0-9]+(\.[0-9]+)?", digit)
    (r"\'[^']{1}\'", character)

    (r"\+|\-|\*|\\|\<|\>|==", operator)
])