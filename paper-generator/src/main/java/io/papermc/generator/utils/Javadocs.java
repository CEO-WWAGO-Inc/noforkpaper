package io.papermc.generator.utils;

public final class Javadocs {

    public static String getVersionDependentClassHeader(String headerIdentifier) {
        return """
        Vanilla keys for %s.
        
        @apiNote The fields provided here are a direct representation of
        what is available from the vanilla game source. They may be
        changed (including removals) on any Minecraft version
        bump, so cross-version compatibility is not provided on the
        same level as it is on most of the other API.
        """.formatted(headerIdentifier);
    }

    public static String getVersionDependentField(String headerIdentifier) {
        return """
        %s
        
        @apiNote This field is version-dependant and may be removed in future Minecraft versions
        """.formatted(headerIdentifier);
    }

    public static final String CREATE_TYPED_KEY_JAVADOC = """
        Creates a key for {@link $T} in a registry.
        
        @param key the value's key in the registry
        @return a new typed key
        """;

    private Javadocs() {
    }
}