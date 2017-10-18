package net.h34t.temporize;

public enum ConditionalType {
    /**
     * the conditional is used as a string placeholder
     */
    STRING,

    /**
     * used as a block identifier
     */
    BLOCK,

    /**
     * used as an include
     */
    INCLUDE,

    /**
     * neither of the others, which will generate a boolean property
     */
    BOOLEAN
}
