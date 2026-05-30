package travelapp.util;

import java.util.Scanner;

public class InputHelper {
    private static final Scanner sc = new Scanner(System.in);

    public static <E extends Enum<E>> E chooseEnum(Class<E> enumClass, String title) {
        E[] values = enumClass.getEnumConstants();
        System.out.println("\n" + title + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.printf("%d. %s%n", i + 1, values[i].name());
        }
        System.out.print("Оберіть номер (або Enter для пропуску): ");
        String input = sc.nextLine().trim();
        if (input.isBlank()) return null;
        try {
            int choice = Integer.parseInt(input);
            if (choice < 1 || choice > values.length) return null;
            return values[choice - 1];
        } catch (Exception e) {
            return null;
        }
    }

    public static double readDouble(String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isBlank()) return 0;
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            System.out.println("Невірне число, використано 0");
            return 0;
        }
    }
}
