package com.andreabergia.synthetic;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator {
    public Class<?> generate(Map<String, Object> properties) throws IllegalAccessException {
        // TODO: cache
        byte[] classBytes = new SingleClassGenerator(properties).generate();
        return MethodHandles.lookup().defineClass(classBytes);
    }

    private static final class SingleClassGenerator {
        private static final AtomicInteger counter = new AtomicInteger();

        private final String generatedClassName;
        private final ClassWriter cw;
        private final Map<String, Object> recordProperties;
        private final Map<String, String> fieldTypes;

        public SingleClassGenerator(Map<String, Object> recordProperties) {
            this.generatedClassName = "com/andreabergia/synthetic/synthesized" + counter.getAndIncrement();
            this.cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            this.recordProperties = recordProperties;
            this.fieldTypes = new HashMap<>(recordProperties.size());
        }

        private byte[] generate() {
            startClass();
            generateFields();
            generateConstructor();
            cw.visitEnd();
            return cw.toByteArray();
        }

        private ClassWriter startClass() {
            cw.visit(
                    V1_7,
                    ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC,
                    generatedClassName,
                    null,
                    "java/lang/Object",
                    new String[0]
            );
            return cw;
        }

        private void generateFields() {
            for (Map.Entry<String, Object> mapEntry : recordProperties.entrySet()) {
                String fieldName = toFieldName(mapEntry.getKey());

                String fieldType;
                if (mapEntry.getValue() == null) {
                    fieldType = "java/lang/Object";
                } else {
                    fieldType = getFieldType(mapEntry.getValue().getClass());
                }

                cw.visitField(ACC_PUBLIC | ACC_FINAL, fieldName, toFieldDescriptor(fieldType), null, null);
                fieldTypes.put(fieldName, fieldType);

                // TODO: add jackson annotation JsonProperty
            }
        }

        private String getFieldType(Class<?> aClass) {
            return aClass.getName().replace(".", "/");
        }

        private void generateConstructor() {
            MethodVisitor mw = cw.visitMethod(
                    ACC_PUBLIC,
                    "<init>",
                    "(Ljava/util/Map;)V",
                    null,
                    null
            );
            mw.visitCode();
            mw.visitVarInsn(ALOAD, 0);
            mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

            fillFieldsValues(mw);

            mw.visitInsn(RETURN);
            mw.visitMaxs(-1, -1);
            mw.visitEnd();
        }

        private void fillFieldsValues(MethodVisitor mw) {
            for (String propertyName : recordProperties.keySet()) {
                String fieldName = toFieldName(propertyName);
                String fieldType = fieldTypes.get(fieldName);

                // Save the current instance of the class on the stack
                mw.visitVarInsn(ALOAD, 0);

                // Invoke map::get
                mw.visitVarInsn(ALOAD, 1);
                mw.visitLdcInsn(propertyName);
                mw.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);

                // Cast result appropriately
                mw.visitTypeInsn(CHECKCAST, fieldType);

                // Save it in the object
                mw.visitFieldInsn(PUTFIELD, generatedClassName, fieldName, toFieldDescriptor(fieldType));
            }
        }

        private String toFieldName(String propertyName) {
            // TODO: normalize any invalid character
            return propertyName;
        }

        private String toFieldDescriptor(String fieldType) {
                return "L" + fieldType + ";";
        }
    }
}
