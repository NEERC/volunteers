package ru.ifmo.neerc.dev;

import javax.persistence.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates .puml file with database diagram.
 */
public class PumlGenerator {

    private static final String INDENT = "    ";
    private static final String ONE = " \"1\"";
    private static final String MANY = " \"∞\"";
    private static final String PACKAGE = "ru.ifmo.neerc.volunteers.entity";

    private static void processClass(
            Class<?> aClass,
            List<? extends Class<?>> classes,
            List<String> connections,
            PrintWriter printWriter) {

        printWriter.println(String.format("class %s {", aClass.getSimpleName()));

        for (Field field : aClass.getDeclaredFields()) {
            Class<?> type = field.getType();

            String typeStr;
            Class<?> referenceType;

            if (Collection.class.isAssignableFrom(type)) {
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                Class<?> collectionType = (Class<?>) parameterizedType.getRawType();
                Class<?> parameterType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                typeStr = String.format("%s<%s>",
                        collectionType.getSimpleName(),
                        parameterType.getSimpleName());
                referenceType = parameterType;
            } else {
                typeStr = type.getSimpleName();
                referenceType = type;
            }

            if (classes.contains(referenceType)) {
                String fromAmount = "";
                if (field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(OneToOne.class)) {
                    fromAmount = ONE;
                } else if (field.isAnnotationPresent(ManyToOne.class) ||
                        field.isAnnotationPresent(ManyToMany.class)) {
                    fromAmount = MANY;
                }

                String toAmount = "";
                if (field.isAnnotationPresent(ManyToOne.class) ||
                        field.isAnnotationPresent(OneToOne.class)) {
                    toAmount = ONE;
                } else if (field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(ManyToMany.class)) {
                    toAmount = MANY;
                }

                connections.add(String.format("%s%s -->%s %s : %s",
                        aClass.getSimpleName(), fromAmount,
                        toAmount, referenceType.getSimpleName(),
                        field.getName()));
            }

            printWriter.println(String.format("%s%s %s", INDENT, typeStr, field.getName()));
        }
        printWriter.println("}");
        printWriter.println();
    }

    private static void processEnum(Class<?> aClass, PrintWriter printWriter) {
        printWriter.println(String.format("enum %s {", aClass.getSimpleName()));
        for (Object value : aClass.getEnumConstants()) {
            printWriter.println(INDENT + value.toString());
        }
        printWriter.println("}");
        printWriter.println();
    }

    /**
     * Entry point.
     * @param args array of one output file name.
     */
    public static void main(String[] args) {
        Path packagePath = Paths.get("src", "main", "java");
        String packageName = PACKAGE;
        packagePath = packagePath.resolve(
                Arrays.stream(packageName.split("\\."))
                        .collect(Collectors.joining("" + File.separator)));
        String[] classNames = packagePath.toFile().list();
        assert classNames != null;

        List<? extends Class<?>> classes = Arrays.stream(classNames)
                .filter(name -> name.matches("^.*\\.java$"))
                .map(name -> name.substring(0, name.length() - 5))
                .map(name -> {
                    try {
                        return Class.forName(packageName + "." + name);
                    } catch (ClassNotFoundException e) {
                        System.err.println("not found " + packageName + "." + name);
                        return null;
                    }
                })
                .filter(aClass -> aClass != null && aClass.isAnnotationPresent(Entity.class))
                .collect(Collectors.toList());

        List<String> connections = new ArrayList<>();

        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                Paths.get(args[0] + ".puml").toFile()),
                StandardCharsets.UTF_8))) {

            printWriter.println("@startuml");
            printWriter.println();

            classes.forEach(aClass -> {
                if (aClass.isEnum()) {
                    processEnum(aClass, printWriter);
                } else {
                    processClass(aClass, classes, connections, printWriter);
                }
            });

            connections.forEach(printWriter::println);

            printWriter.println();
            printWriter.println("@enduml");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
