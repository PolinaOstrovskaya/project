import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

class Account {
    private String number;
    private double balance;

    public Account(String number, double balance) {
        this.number = number;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void transfer(Account to, double amount) throws IOException {
        if (amount > 0 && amount <= this.balance) {
            this.balance -= amount;
            to.balance += amount;
            writeReport(this.number, to.number, amount);
            System.out.println("Перевод выполнен успешно.");
        } else {
            System.out.println("Перевод невозможен. Проверьте сумму и баланс счетов.");
        }
    }

    public static void writeReport(String from, String to, double amount) throws IOException {
        // Создаем объект для работы с файлом-отчетом
        File report = new File("report.txt");
        // Создаем объект для записи в файл
        FileWriter writer = new FileWriter(report); // второй параметр означает, что мы дописываем в конец файла, а не перезаписываем его
        // Записываем информацию о переводе в формате: "счет1 -> счет2: сумма"
        writer.write(LocalDateTime.now().toString() + "|" + from + " | " + to + " | " + amount +" успешно\n" +
                "обработан"+ "|"+ "\n");
        // Закрываем поток записи
        writer.close();
    }
}

public class MoneyTransfer extends Account {

    public static final String ACCOUNTS_FILE = "accounts.txt";
    public static final String INPUT_DIR = "input";

    public static final String ARCHIVE_DIR = "archive";

    public static Map<String, Account> accounts = new HashMap<>();

    public MoneyTransfer(String number, double balance) {
        super(number, balance);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        loadAccounts();
        System.out.println("Добро пожаловать в программу для выполнения денежного перевода.");
        System.out.println("Выберите операцию:");
        System.out.println("1 - вызов операции парсинга файлов перевода из input");
        System.out.println("2 - вызов операции вывода списка всех переводов из файла-отчета");
        System.out.println("3 - выход из программы");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                parseFiles();
                break;
            case 2:
                showReport();
                break;
            case 3:
                System.out.println("Спасибо за использование программы. До свидания.");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор. Попробуйте еще раз.");
                break;
        }
        scanner.close();
    }

    private static void showReport() {
        try {
            File report = new File("report.txt");
            if (report.exists()) {
                Scanner reader = new Scanner(report);
                System.out.println("Список всех переводов:");
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    System.out.println(line);
                }
                reader.close();
            } else {
                System.out.println("Файл-отчет не найден: " + report.getName());
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при выводе отчета: " + e.getMessage());
        }
    }

    public static void loadAccounts() {
        try {
            File file = new File(ACCOUNTS_FILE);
            if (file.exists()) {
                Scanner reader = new Scanner(file);
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    String[] parts = line.split(";");
                    if (parts.length == 2) {
                        String number = parts[0];
                        double balance = Double.parseDouble(parts[1]);
                        Account account = new Account(number, balance);
                        accounts.put(number, account);
                    } else {
                        System.out.println("Неверный формат данных в файле счетов: " + line);
                    }
                }
                reader.close();
            } else {
                System.out.println("Файл счетов не найден: " + ACCOUNTS_FILE);
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при загрузке счетов: " + e.getMessage());
        }
    }

    public static void parseFiles() {
        try {
            File dir = new File(INPUT_DIR);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                files = Arrays.stream(files).filter(f -> f.getName().endsWith(".txt")).toArray(File[]::new);
                if (files.length > 0) {
                    for (File file : files) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        String[] parts = content.split(";");
                        if (parts.length == 3) {
                            String from = parts[0];
                            String to = parts[1];
                            double amount = Double.parseDouble(parts[2]);
                            if (isValidAccount(from) && isValidAccount(to) && amount > 0) {
                                Account fromAccount = accounts.get(from);
                                Account toAccount = accounts.get(to);
                                fromAccount.transfer(toAccount, amount);
                            } else {
                                System.out.println("Неверный формат данных в файле перевода: " + file.getName());
                            }
                        } else {
                            System.out.println("Неверный формат данных в файле перевода: " + file.getName());
                        }
                        Files.move(file.toPath(), Paths.get(ARCHIVE_DIR, file.getName()));
                    }
                } else {
                    System.out.println("Нет файлов для парсинга в каталоге input");
                }
            } else {
                System.out.println("Каталог input не найден");
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка при парсинге файлов: " + e.getMessage());
        }
    }

    public static boolean isValidAccount(String number) {
        return number.matches("\\d{5}-\\d{5}") && accounts.containsKey(number);
    }}