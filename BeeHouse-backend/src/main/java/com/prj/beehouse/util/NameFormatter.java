package com.prj.beehouse.util;

import java.util.Locale;

/**
 * Formats person names into their canonical DISPLAY form:
 * {@code <Title> <FirstName> <LastName>}, where every word of the first and
 * last name starts with an uppercase letter followed by lowercase letters.
 * <p>
 * Handles compound surnames ("de luca" → "De Luca"), apostrophes
 * ("d'amico" → "D'Amico"), hyphens ("anna-maria" → "Anna-Maria") and accented
 * characters. All case conversions use {@link Locale#ITALIAN}, never the
 * default locale.
 * <p>
 * Team convention: the database stores everything LOWERCASE
 * ({@link #normalizeForStorage(String)} on the way in); the canonical
 * capitalization is applied only on the way out, in DTOs/emails/PDFs
 * ({@link #normalize(String)} / {@link #fullName(String, String, String)}).
 */
public final class NameFormatter {

    private NameFormatter() {
    }

    /**
     * Normalizes a name for STORAGE, per team convention: trims, collapses
     * runs of whitespace to a single space and lowercases everything with
     * {@link Locale#ITALIAN}. Display capitalization is applied on output
     * only, via {@link #normalize(String)}.
     * <p>
     * Null-safe: returns {@code null} for {@code null} input.
     *
     * @param raw the name as typed, may be null
     * @return the lowercase storage form, {@code null} if the input was null
     */
    public static String normalizeForStorage(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ITALIAN);
    }

    /**
     * Normalizes a first or last name: trims, collapses runs of whitespace to
     * a single space, lowercases everything and re-capitalizes the first
     * letter of each word — a new word starts after a space, an apostrophe or
     * a hyphen.
     * <p>
     * Null-safe: returns {@code null} for {@code null} input and {@code ""}
     * for blank input.
     *
     * @param raw the name as typed/stored, may be null
     * @return the normalized name, {@code null} if the input was null
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ITALIAN);
        if (cleaned.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(cleaned.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (capitalizeNext && Character.isLetter(c)) {
                out.append(String.valueOf(c).toUpperCase(Locale.ITALIAN));
                capitalizeNext = false;
            } else {
                out.append(c);
                if (c == ' ' || c == '\'' || c == '-') {
                    capitalizeNext = true;
                }
            }
        }
        return out.toString();
    }

    /**
     * Builds the canonical display name {@code <Title> <FirstName> <LastName>}
     * (e.g. "Dott.ssa Camilla Zampetti"). The title is never invented, only
     * re-cased via {@link #formatTitle(String)} (stored lowercase per team
     * convention); first and last name are {@link #normalize(String)}d.
     * Null or blank parts are skipped, so a missing title yields
     * "FirstName LastName".
     *
     * @param title     the stored title (e.g. "dott.", "dott.ssa"), may be null
     * @param firstName the first name, may be null
     * @param lastName  the last name, may be null
     * @return the assembled display name, {@code ""} when every part is blank
     */
    public static String fullName(String title, String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        appendPart(sb, formatTitle(title));
        appendPart(sb, normalize(firstName));
        appendPart(sb, normalize(lastName));
        return sb.toString();
    }

    /**
     * Renders a stored professional title for display: trims and capitalizes
     * the first letter, lowercasing the rest ("dott.ssa" → "Dott.ssa").
     * Never invents a title: null stays null, blank becomes "".
     *
     * @param title the stored title, may be null
     * @return the display form of the title
     */
    public static String formatTitle(String title) {
        if (title == null) {
            return null;
        }
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ITALIAN)
                + trimmed.substring(1).toLowerCase(Locale.ITALIAN);
    }

    /** Appends a part preceded by a single space, skipping null/empty parts. */
    private static void appendPart(StringBuilder sb, String part) {
        if (part == null || part.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(part);
    }
}
