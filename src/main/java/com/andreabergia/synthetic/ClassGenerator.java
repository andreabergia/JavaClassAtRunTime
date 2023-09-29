package com.andreabergia.synthetic;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class ClassGenerator {
    private static final AtomicInteger counter = new AtomicInteger();

    public Class<?> generate(Map<String, Object> properties) throws IllegalAccessException {
        byte[] classBytes = generateClass();
        return MethodHandles.lookup().defineClass(classBytes);
    }

    private byte[] generateClass() {
        ClassWriter cw = startClass();
        generateConstructor(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }

    private ClassWriter startClass() {
        ClassWriter cw  = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)  ;
        cw.visit(
                V1_7,
                ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC,
                "com/andreabergia/synthetic/synthesized" + counter.getAndIncrement(),
                null,
                "java/lang/Object",
                new String[0]
        );
        return cw;
    }

    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mw = cw.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null
        );
        mw.visitCode();
        mw.visitVarInsn(ALOAD, 0);
        mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mw.visitInsn(RETURN);
        mw.visitMaxs(-1, -1);
        mw.visitEnd();
    }
}
