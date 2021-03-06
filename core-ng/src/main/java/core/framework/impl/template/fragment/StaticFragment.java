package core.framework.impl.template.fragment;

import core.framework.impl.template.TemplateContext;

/**
 * @author neo
 */
public class StaticFragment implements Fragment {
    private final StringBuilder content = new StringBuilder();

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        builder.append(content);
    }

    void append(String content) {
        this.content.append(content);
    }
}
