package com.chenkuojun.mytomcat.logging.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import com.chenkuojun.mytomcat.logging.ansi.AnsiColor;
import com.chenkuojun.mytomcat.logging.ansi.AnsiElement;
import com.chenkuojun.mytomcat.logging.ansi.AnsiOutput;
import com.chenkuojun.mytomcat.logging.ansi.AnsiStyle;

import java.util.Map;

/**
 * Logback {@link CompositeConverter} colors output using the {@link AnsiOutput} class. A
 * single 'color' option can be provided to the converter, or if not specified color will
 * be picked based on the logging level.
 *
 * @author Phillip Webb
 * @since 1.0.1
 */
public class ColorConverter extends CompositeConverter<ILoggingEvent> {

    private static final Map<String, AnsiElement> ELEMENTS;

    static {
        ELEMENTS = Map.of("faint", AnsiStyle.FAINT, "red", AnsiColor.RED, "green", AnsiColor.GREEN, "yellow", AnsiColor.YELLOW, "blue", AnsiColor.BLUE, "magenta", AnsiColor.MAGENTA, "cyan", AnsiColor.CYAN);
    }

    private static final Map<Integer, AnsiElement> LEVELS;

    static {
        LEVELS = Map.of(Level.ERROR_INTEGER, AnsiColor.RED, Level.WARN_INTEGER, AnsiColor.YELLOW);
    }

    @Override
    protected String transform(ILoggingEvent event, String in) {
        AnsiElement element = ELEMENTS.get(getFirstOption());
        if (element == null) {
            // Assume highlighting
            element = LEVELS.get(event.getLevel().toInteger());
            element = (element != null) ? element : AnsiColor.GREEN;
        }
        return toAnsiString(in, element);
    }

    protected String toAnsiString(String in, AnsiElement element) {
        return AnsiOutput.toString(element, in);
    }

}
