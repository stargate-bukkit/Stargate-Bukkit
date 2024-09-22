package org.sgrewritten.stargate;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockBukkitExtension;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.sgrewritten.stargate.api.gate.GateFormatRegistry;
import org.sgrewritten.stargate.gate.GateFormatHandler;
import org.sgrewritten.stargate.thread.task.StargateQueuedAsyncTask;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StargateExtension extends MockBukkitExtension {

    private static long id = -1;
    private static final String SERVER_NAME = "test_server";
    private static final File TEST_GATES_DIR = new File("src/test/resources/gates");
    private boolean hadPluginField;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        super.beforeEach(context);
        id++;
        System.setProperty("bstats.relocatecheck", "false");
        Stargate.setServerName(SERVER_NAME);
        this.hadPluginField = injectStargateField(context);
        if (!hadPluginField) {
            StargateQueuedAsyncTask.enableAsyncQueue(id);
            GateFormatRegistry.setFormats(Objects.requireNonNull(GateFormatHandler.loadGateFormats(TEST_GATES_DIR)));
        }
    }

    private boolean injectStargateField(ExtensionContext context) throws IllegalAccessException {
        Optional<Class<?>> classOptional = context.getTestClass();
        if (classOptional.isEmpty()) {
            return false;
        }

        List<Field> serverMockFields = FieldUtils.getAllFieldsList(classOptional.get())
                .stream()
                .filter(field -> field.getType() == Stargate.class)
                .filter(field -> field.getAnnotation(StargateInject.class) != null)
                .toList();

        Optional<Object> optionalTestInstance = context.getTestInstance();
        if (optionalTestInstance.isEmpty()) {
            return false;
        }

        Object testInstance = optionalTestInstance.get();
        for (Field field : serverMockFields) {
            String name = field.getName();
            Stargate stargate = MockBukkit.load(Stargate.class);
            FieldUtils.writeDeclaredField(testInstance, name, stargate, true);
        }
        return !serverMockFields.isEmpty();
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (!hadPluginField) {
            StargateQueuedAsyncTask.disableAsyncQueue(id);
        }
        super.afterEach(context);
    }

    private boolean isStargatePluginParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        final boolean paramIsCorrectType = parameterContext.getParameter().getType() == Stargate.class;
        final boolean paramHasCorrectAnnotation = parameterContext.isAnnotated(StargateInject.class);
        return paramIsCorrectType && paramHasCorrectAnnotation;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isStargatePluginParameter(parameterContext, extensionContext) || super.supportsParameter(parameterContext, extensionContext);
    }


    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (isStargatePluginParameter(parameterContext, extensionContext)) {
            return MockBukkit.load(Stargate.class);
        }
        return super.resolveParameter(parameterContext, extensionContext);
    }
}
