package core.framework.impl.log.filter;

import java.util.Map;
import java.util.Set;

/**
 * @author neo
 */
public class MapLogParam implements LogParam {
    private final Map<String, String> values;

    public MapLogParam(Map<String, String> values) {
        this.values = values;
    }

    @Override
    public void append(StringBuilder builder, Set<String> maskedFields, int maxParamLength) {
        int maxLength = builder.length() + maxParamLength;
        builder.append('{');
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (index > 0) builder.append(", ");
            String key = entry.getKey();
            builder.append(key).append('=');

            if (maskedFields.contains(key)) builder.append("******");
            else builder.append(entry.getValue());

            if (builder.length() >= maxLength) {
                builder.setLength(maxLength);
                builder.append("...(truncated)");
                return;
            }

            index++;
        }
        builder.append('}');
    }
}
