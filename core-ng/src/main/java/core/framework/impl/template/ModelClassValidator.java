package core.framework.impl.template;

import core.framework.impl.reflect.Methods;
import core.framework.impl.validate.type.DataTypeValidator;
import core.framework.impl.validate.type.TypeVisitor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
class ModelClassValidator implements TypeVisitor {
    private final DataTypeValidator validator;

    ModelClassValidator(Class<?> modelClass) {
        validator = new DataTypeValidator(modelClass);
        validator.allowChild = true;
        validator.visitor = this;
    }

    void validate() {
        validator.validate();
    }

    @Override
    public void visitClass(Class<?> objectClass, String path) {
        Method[] methods = objectClass.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && method.getReturnType().isPrimitive()) {
                throw new Error(format("primitive class as return type is not supported, please use object type, returnType={}, method={}", method.getReturnType(), Methods.path(method)));
            }
        }
    }
}
