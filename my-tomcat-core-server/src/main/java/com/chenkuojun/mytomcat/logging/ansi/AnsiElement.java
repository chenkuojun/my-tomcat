package com.chenkuojun.mytomcat.logging.ansi;


/**
 * An ANSI encodeAble element.
 *
 * @author Phillip Webb
 * @since 1.0.1
 */
public interface AnsiElement {

    /**
     * return the ANSI escape code.
     *
     * @return the ANSI escape code
     */
    @Override
    String toString();

}
