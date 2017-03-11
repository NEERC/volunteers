package ru.ifmo.neerc.volunteers.entity;

import javax.persistence.Entity;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PumlGenerator {
    public static void main(String[] args) {
        Path packagePath = Paths.get("src", "main", "java");
        String packageName = PumlGenerator.class.getPackage().getName();
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
                        System.out.println("not found " + packageName + "." + name);
                        return null;
                    }
                })
                .filter(aClass -> aClass != null && aClass.isAnnotationPresent(Entity.class))
                .collect(Collectors.toList());

        try (PrintWriter printWriter = new PrintWriter(Paths.get(args[0] + ".puml").toFile())) {

            printWriter.println("@startuml");
            printWriter.println();

            classes.forEach(aClass -> {
                printWriter.println(String.format("class %s {", aClass.getSimpleName()));
                for (Field field : aClass.getDeclaredFields()) {
                    Class<?> type = field.getType();
                    String typeStr;
                    if (Collection.class.isAssignableFrom(type)) {
                        typeStr = field.getGenericType().getTypeName()
                                .replaceAll("[^<]*\\.", "");
                        System.out.println(typeStr);
                    } else {
                        typeStr = type.getSimpleName();
                    }
                    printWriter.println(String.format("%s %s", typeStr, field.getName()));
                }
                printWriter.println("}");
                printWriter.println();
            });

            printWriter.println("@enduml");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
