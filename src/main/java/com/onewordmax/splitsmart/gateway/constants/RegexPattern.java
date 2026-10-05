package com.onewordmax.splitsmart.gateway.constants;

public class RegexPattern {
    // \p{L} = Any Unicode letter, \p{M} = marks (accents), \p{Zs} = separator (spaces)
    public static final String NAME = "(?U)^[\\p{L}\\p{M}\\p{Zs}'’\\-.]{2,100}$";

    // Standard alphanumeric username
    public static final String USERNAME = "^[A-Za-z0-9_]{3,30}$";

    // Basic email check
    public static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    // Password: 1 Unicode Letter, 1 Number, 8-64 characters
    public static final String PASSWORD = "^(?=.*\\p{L})(?=.*\\p{N}).{8,64}$";

    // Format checks (Existence is checked via Java Locale/Currency classes)
    public static final String COUNTRY = "^[A-Z]{2}$";
    public static final String CURRENCY = "^[A-Z]{3}$";
    public static final String LANGUAGE = "^[a-z]{2}$";

    public static final String JWT = "^[A-Za-z0-9_=-]+\\.[A-Za-z0-9_=-]+\\.[A-Za-z0-9_=-]+$";
}