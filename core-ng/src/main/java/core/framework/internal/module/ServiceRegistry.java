package core.framework.internal.module;

import core.framework.internal.bean.BeanClassNameValidator;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author neo
 */
public class ServiceRegistry {
    public final Set<Class<?>> serviceInterfaces = new LinkedHashSet<>();
    public BeanClassNameValidator beanClassNameValidator = new BeanClassNameValidator();
}
