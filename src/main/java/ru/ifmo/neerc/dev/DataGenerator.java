package ru.ifmo.neerc.dev;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * Created by Lapenok Akesej on 08.04.2017.
 */
public class DataGenerator {

    private static final String TABLE_USER = "user";
    private static final String TABLE_YEAR = "year";
    private static final String TABLE_APPLICATION_FORM = "application_form";

    private static final String[] COL_USER = new String[]{
            "id", "badge_name", "badge_name_cyr", "email", "first_name", "first_name_cyr", "last_name", "last_name_cyr", "password", "role_id"
    };
    private static final Class[] COL_TYPE = new Class[]{
            Integer.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, Integer.class
    };

    private static final int COUNT_USER = 20;

    public static void main(String[] args) {
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                Paths.get(args[0]).toFile()),
                StandardCharsets.UTF_8))) {

            printWriter.println("-- write users");

            printWriter.print("INSERT IGNORE INTO ");
            printWriter.println(TABLE_USER);
            printWriter.print("(");
            for (int i = 0; i < COL_USER.length; i++) {
                printWriter.print(COL_USER[i]);
                if (i != COL_USER.length - 1)
                    printWriter.print(", ");
            }
            printWriter.println(") VALUES");
            for (int i = 0; i < COUNT_USER; i++) {
                printWriter.print("(");
                for (int j = 0; j < COL_USER.length; j++) {
                    if (COL_TYPE[j] == String.class) {
                        printWriter.print("'user" + i + "'");
                    } else {
                        if (COL_USER[j].equals("id"))
                            printWriter.print(i + 2);
                        else
                            printWriter.print(2);
                    }
                    if (j != COL_USER.length - 1) {
                        printWriter.print(", ");
                    }
                }
                if (i != COUNT_USER - 1)
                    printWriter.println("),");
                else
                    printWriter.println(");");
            }
            printWriter.println("-- write year");
            printWriter.println("INSERT IGNORE INTO " + TABLE_YEAR);
            printWriter.println("(id, name, open_for_registration) VALUE");
            printWriter.println("(1,'2016',TRUE);");

            printWriter.println("-- write application form");
            printWriter.println("INSERT IGNORE INTO " + TABLE_APPLICATION_FORM);
            printWriter.println("(id,`group`,suggestions,user_id,year) VALUES");
            for (int i = 0; i < COUNT_USER; i++) {
                printWriter.print("(");
                printWriter.print(i + 1);
                printWriter.print(", 'M333" + (4 + i % 5));
                printWriter.print("', 'no suggestion', ");
                printWriter.print(i + 2);
                printWriter.print(", 1)");
                if (i == COUNT_USER - 1) {
                    printWriter.println(";");
                } else {
                    printWriter.println(",");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
