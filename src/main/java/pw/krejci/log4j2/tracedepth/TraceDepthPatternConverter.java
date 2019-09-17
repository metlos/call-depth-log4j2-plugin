package pw.krejci.log4j2.tracedepth;

import static org.apache.logging.log4j.spi.AbstractLogger.ENTRY_MARKER;
import static org.apache.logging.log4j.spi.AbstractLogger.EXIT_MARKER;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name = "TraceDepthPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"traceDepth"})
@PerformanceSensitive("allocation")
public final class TraceDepthPatternConverter extends LogEventPatternConverter {
    private final ThreadLocal<Integer> traceDepth = ThreadLocal.withInitial(() -> 0);
    private final String indentStep;

    private TraceDepthPatternConverter(String[] options) {
        super("TraceDepth", "trace-depth");

        if (options == null || options.length != 1) {
            indentStep = "  ";
        } else {
            int length;
            try {
                length = Integer.parseInt(options[0]);
            } catch (NumberFormatException e) {
                indentStep = "  ";
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; ++i) {
                sb.append(' ');
            }
            indentStep = sb.toString();
        }
    }

    @SuppressWarnings("unused")
    public static TraceDepthPatternConverter newInstance(String[] options) {
        return new TraceDepthPatternConverter(options);
    }

    public void format(LogEvent event, StringBuilder toAppendTo) {
        if (event.getLevel() != Level.TRACE) {
            return;
        }

        int depth = traceDepth.get();

        if (event.getMarker() == EXIT_MARKER) {
            traceDepth.set(--depth);
        }

        for (int i = 0; i < depth; ++i) {
            toAppendTo.append(indentStep);
        }

        if (event.getMarker() == ENTRY_MARKER) {
            traceDepth.set(depth + 1);
        }
    }
}
