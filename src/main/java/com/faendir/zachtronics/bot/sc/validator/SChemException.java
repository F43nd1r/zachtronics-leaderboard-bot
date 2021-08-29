package com.faendir.zachtronics.bot.sc.validator;

class SChemException extends RuntimeException {
    SChemException(String message) {
        super(message);
    }

    SChemException(String message, Throwable cause) {
        super(message, cause);
    }
}
