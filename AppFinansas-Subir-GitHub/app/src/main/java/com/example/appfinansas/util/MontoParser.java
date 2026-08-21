package com.example.appfinansas.util;

public final class MontoParser {

    private MontoParser() {
    }

    /**
     * Convierte texto a monto. Soporta formatos como 25.50, 25,50 y 100.000 (mil).
     */
    public static double parse(String texto) throws NumberFormatException {
        if (texto == null) {
            throw new NumberFormatException("Monto vacío");
        }

        String limpio = texto.trim()
                .replace("S/", "")
                .replace("s/", "")
                .replace(" ", "");

        if (limpio.isEmpty()) {
            throw new NumberFormatException("Monto vacío");
        }

        if (limpio.contains(",") && limpio.contains(".")) {
            limpio = limpio.replace(".", "").replace(",", ".");
        } else if (limpio.contains(",")) {
            limpio = limpio.replace(",", ".");
        } else if (esSeparadorDeMiles(limpio)) {
            limpio = limpio.replace(".", "");
        }

        return Double.parseDouble(limpio);
    }

    private static boolean esSeparadorDeMiles(String valor) {
        if (!valor.contains(".")) {
            return false;
        }
        return valor.matches("\\d{1,3}(\\.\\d{3})+");
    }
}
