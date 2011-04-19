/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.restsimple.creator;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.sonatype.restsimple.api.Action;
import org.sonatype.restsimple.api.DefaultServiceDefinition;
import org.sonatype.restsimple.api.DeleteServiceHandler;
import org.sonatype.restsimple.api.GetServiceHandler;
import org.sonatype.restsimple.api.MediaType;
import org.sonatype.restsimple.api.PostServiceHandler;
import org.sonatype.restsimple.api.PutServiceHandler;
import org.sonatype.restsimple.api.ServiceDefinition;
import org.sonatype.restsimple.api.ServiceHandler;

import javax.inject.Named;
import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// ------------------------------
// Method   URL        Action
// ------------------------------
// POST     /users     create
// GET      /users     read
// GET      /users/23  read
// PUT      /users/23  update
// DESTROY  /users/23  delete

@Named
@Singleton
public class MethodBasedServiceDefintionCreator
        implements org.sonatype.restsimple.creator.ServiceDefinitionCreator {

    public final static String APPLICATION = "application";
    public final static String JSON = "json";
    public final static String XML = "xml";

    private final MediaType APPLICATION_JSON = new MediaType(APPLICATION, JSON);

    public ServiceDefinition create(Class<?> application) throws Exception {
        ServiceDefinition serviceDefinition = new DefaultServiceDefinition();

        Method[] methods = application.getDeclaredMethods();

        for (Method method : methods) {
            ServiceHandler serviceHandler = null;
            Class[] types = method.getParameterTypes();

            if (method.getName().startsWith("create")) {
                serviceHandler = new PostServiceHandler("/users", GenericActionDump.generate(application, method));
            }

            if (method.getName().startsWith("read")) {

                if (types.length == 0) {
                    serviceHandler = new GetServiceHandler("/users", GenericActionDump.generate(application, method));
                } else if (types.length == 1) {
                    serviceHandler = new GetServiceHandler("/user", GenericActionDump.generate(application, method));
                }
            }

            if (method.getName().startsWith("update")) {
                serviceHandler = new PutServiceHandler("/users", GenericActionDump.generate(application, method));
            }

            if (method.getName().startsWith("delete")) {
                serviceHandler = new DeleteServiceHandler("/user", GenericActionDump.generate(application, method));
            }

            if (serviceHandler == null) {
                throw new IllegalStateException("Unable to map a service");
            }

            if (types.length == 0) {
                serviceHandler.producing(APPLICATION_JSON);
            } else if (types.length == 1) {
                serviceHandler.consumeWith(APPLICATION_JSON, types[0]).producing(APPLICATION_JSON);
            }
            serviceDefinition.withHandler(serviceHandler);
        }
        return serviceDefinition;
    }


    public final static class GenericActionDump implements Opcodes {

        public static <T> Action generate(Class<T> clazz, Method method) throws Exception {

            ClassWriter cw = new ClassWriter(0);
            FieldVisitor fv;
            MethodVisitor mv;
            AnnotationVisitor av0;
            Object instance = clazz.newInstance();
            String className = clazz.getName().replace(".", "/") + "Action";

            String returnType = method.getReturnType().getName().replace(".", "/");
            String parameterType = "java/lang/Object";
            if (method.getParameterTypes().length > 0) {
                parameterType = method.getParameterTypes()[0].getName().replace(".", "/");
            }

            cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, className, "Ljava/lang/Object;Lorg/sonatype/restsimple/api/Action<L"
                    + returnType + ";L"
                    + parameterType + ";>;", "java/lang/Object", new String[]{"org/sonatype/restsimple/api/Action"});

            {
                fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "object", "Ljava/lang/Object;", null, null);
                fv.visitEnd();
            }
            {
                fv = cw.visitField(ACC_PRIVATE + ACC_FINAL, "method", "Ljava/lang/reflect/Method;", null, null);
                fv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/Object;Ljava/lang/reflect/Method;)V", null, null);
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(PUTFIELD, className, "object", "Ljava/lang/Object;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitFieldInsn(PUTFIELD, className, "method", "Ljava/lang/reflect/Method;");
                mv.visitInsn(RETURN);
                mv.visitMaxs(2, 3);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC, "action", "(Lorg/sonatype/restsimple/api/ActionContext;)L" + returnType + ";",
                        "(Lorg/sonatype/restsimple/api/ActionContext<L" + parameterType + ";>;)L" + returnType + ";", new String[]{"org/sonatype/restsimple/api/ActionException"});
                mv.visitCode();
                Label l0 = new Label();
                Label l1 = new Label();
                Label l2 = new Label();
                mv.visitTryCatchBlock(l0, l1, l2, "java/lang/IllegalAccessException");
                Label l3 = new Label();
                mv.visitTryCatchBlock(l0, l1, l3, "java/lang/reflect/InvocationTargetException");
                mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, "method", "Ljava/lang/reflect/Method;");
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, "object", "Ljava/lang/Object;");
                if (method.getParameterTypes().length > 0) {
                    mv.visitInsn(ICONST_1);
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                    mv.visitInsn(DUP);
                    mv.visitInsn(ICONST_0);
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "org/sonatype/restsimple/api/ActionContext", "get", "()Ljava/lang/Object;");
                    mv.visitInsn(AASTORE);
                } else {
                    mv.visitInsn(ICONST_0);
                    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, "" + returnType + "");
                mv.visitLabel(l1);
                mv.visitInsn(ARETURN);
                mv.visitLabel(l2);
                mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/IllegalAccessException"});
                mv.visitVarInsn(ASTORE, 2);
                mv.visitTypeInsn(NEW, "org/sonatype/restsimple/api/ActionException");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/restsimple/api/ActionException", "<init>", "(Ljava/lang/Throwable;)V");
                mv.visitInsn(ATHROW);
                mv.visitLabel(l3);
                mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[]{"java/lang/reflect/InvocationTargetException"});
                mv.visitVarInsn(ASTORE, 2);
                mv.visitTypeInsn(NEW, "org/sonatype/restsimple/api/ActionException");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKESPECIAL, "org/sonatype/restsimple/api/ActionException", "<init>", "(Ljava/lang/Throwable;)V");
                mv.visitInsn(ATHROW);
                mv.visitMaxs(6, 3);
                mv.visitEnd();
            }
            {
                mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC, "action", "(Lorg/sonatype/restsimple/api/ActionContext;)Ljava/lang/Object;", null, new String[]{"org/sonatype/restsimple/api/ActionException"});
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, className, "action", "(Lorg/sonatype/restsimple/api/ActionContext;)L" + returnType + ";");
                mv.visitInsn(ARETURN);
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
            cw.visitEnd();


            byte[] bytes = cw.toByteArray();

            try {
                String classToLoad = className.replace("/", ".");
                ClassLoader cl = new ByteClassloader(bytes, GenericActionDump.class.getClassLoader(), classToLoad);
                Class<? extends Action> newClazz = (Class<? extends Action>) cl.loadClass(classToLoad);

                Constructor<? extends Action> c = newClazz.getConstructor(new Class[]{Object.class, Method.class});
                return c.newInstance(new Object[]{instance, method});
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

        private final static class ByteClassloader extends ClassLoader {

            private final byte[] clazzBytes;
            private final String className;

            protected ByteClassloader(byte[] clazzBytes, ClassLoader parent, String className) {
                super(parent);
                this.clazzBytes = clazzBytes;
                this.className = className;
            }

            protected Class findClass(String name) throws ClassNotFoundException {

                if (name.compareTo(className) == 0) {
                    return defineClass(name, clazzBytes, 0, clazzBytes.length);
                } else {
                    return super.findClass(name);
                }
            }
        }
    }
}

