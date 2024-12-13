import java.io.*;
import java.util.*;

//class LibraryManagementSystem
public class LibraryManagementSystem {
    public static void main(String[] args) {
        Authenticate Authenticate = new Authenticate();
        Library library = new Library();
        Scanner scanner = new Scanner(System.in);

        Authenticate.loadUsers();
        library.loadBooks();

        while (true) {
            System.out.println("\n--- Welcome to the Library Management System ---");
            System.out.println("1. Register\n2. Login\n3. View Books (Guest)\n4. Buy Book (Guest)\n5. Exit");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> Authenticate.registerUser();
                case 2 -> {
                    User user = Authenticate.loginUser();
                    if (user != null) user.showMenu(library);
                }
                case 3 -> library.viewAllBooks();
                case 4 -> {
                    System.out.print("Enter ISBN of the book to buy: ");
                    library.buyBook(scanner.nextLine());
                }
                case 5 -> {
                    Authenticate.saveUsers();
                    library.saveBooks();
                    System.out.println("Exiting the system...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}

// class User
abstract class User implements Serializable {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }

    public boolean validatePassword(String inputPassword) { return password.equals(inputPassword); }

    public abstract String getRole();

    public abstract void showMenu(Library library);
}

//class librarian
class Librarian extends User {
    public Librarian(String username, String password) { super(username, password); }

    @Override
    public String getRole() {
        return "Librarian";
    }

    @Override
    public void showMenu(Library library) {
        System.out.println("\n--- Librarian Menu ---");
        System.out.println("1. Add Book\n2. Remove Book\n3. View All Books\n4. Logout");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> library.addBook();
                case 2 -> {
                    System.out.print("Enter ISBN of the book to remove: ");
                    library.removeBook(scanner.nextLine());
                }
                case 3 -> library.viewAllBooks();
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}

// class Member
class Member extends User {
    public Member(String username, String password) { super(username, password); }

    @Override
    public String getRole() {
        return "Member";
    }

    @Override
    public void showMenu(Library library) {
        System.out.println("\n--- Member Menu ---");
        System.out.println("1. Borrow Book\n2. Return Book\n3. View All Books\n4. Logout");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.print("Enter ISBN of the book to borrow: ");
                    library.borrowBook(scanner.nextLine());
                }
                case 2 -> {
                    System.out.print("Enter ISBN of the book to return: ");
                    library.returnBook(scanner.nextLine());
                }
                case 3 -> library.viewAllBooks();
                case 4 -> {
                    System.out.println("Logging out...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}

// class Book
class Book implements Serializable {
    private final String title;
    private final String author;
    private final String isbn;
    private boolean isAvailable = true;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
    }

    public String getIsbn() { return isbn; }

    public boolean isAvailable() { return isAvailable; }

    public void setAvailable(boolean available) { isAvailable = available; }

    @Override
    public String toString() {
        return "Title: " + title + ", Author: " + author + ", ISBN: " + isbn + ", Available: " + isAvailable;
    }
}

// class library
class Library {
    private final List<Book> books = new ArrayList<>();

    public void addBook() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter book author: ");
        String author = scanner.nextLine();
        System.out.print("Enter book ISBN: ");
        books.add(new Book(title, author, scanner.nextLine()));
        System.out.println("Book added successfully!");
    }

    public void removeBook(String isbn) {
        books.removeIf(book -> book.getIsbn().equals(isbn));
        System.out.println("Book removed successfully!");
    }

    public void viewAllBooks() {
        System.out.println("\n--- List of Available Books ---");
        books.forEach(System.out::println);
    }

    public void borrowBook(String isbn) {
        books.stream().filter(book -> book.getIsbn().equals(isbn) && book.isAvailable()).findFirst().ifPresentOrElse(
                book -> {
                    book.setAvailable(false);
                    System.out.println("Book borrowed successfully!");
                },
                () -> System.out.println("Book not available."));
    }

    public void returnBook(String isbn) {
        books.stream().filter(book -> book.getIsbn().equals(isbn) && !book.isAvailable()).findFirst().ifPresentOrElse(
                book -> {
                    book.setAvailable(true);
                    System.out.println("Book returned successfully!");
                },
                () -> System.out.println("Invalid return."));
    }

    public void buyBook(String isbn) {
        if (books.removeIf(book -> book.getIsbn().equals(isbn))) {
            System.out.println("Book bought successfully!");
        } else {
            System.out.println("Book not found.");
        }
    }

    public void loadBooks() {
        loadData("books.dat", books);
    }

    public void saveBooks() {
        saveData("books.dat", books);
    }

    @SuppressWarnings("unchecked")
    private <T> void loadData(String fileName, List<T> list) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            list.addAll((List<T>) ois.readObject());
            System.out.println("Data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved data found.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data.");
        }
    }

    private <T> void saveData(String fileName, List<T> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }
}

// class Authenticate
class Authenticate {
    private final List<User> users = new ArrayList<>();

    public void registerUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Register as:\n1. Member\n2. Librarian");
        System.out.print("Enter your choice: ");

        int roleChoice;
        try {
            roleChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (roleChoice == 1) {
            users.add(new Member(username, password));
            System.out.println("Member registered successfully!");
        } else if (roleChoice == 2) {
            users.add(new Librarian(username, password));
            System.out.println("Librarian registered successfully!");
        } else {
            System.out.println("Invalid choice. Registration failed.");
        }
    }

    public User loginUser() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Login as:\n1. Member\n2. Librarian");
        System.out.print("Enter your choice: ");
        int roleChoice;

        try {
            roleChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return null;
        }

        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        for (User user : users) {
            if (user.getUsername().equals(username) && user.validatePassword(password)) {
                if ((roleChoice == 1 && user instanceof Member) || (roleChoice == 2 && user instanceof Librarian)) {
                    System.out.println("Login successful!");
                    return user;
                } else {
                    System.out.println("Incorrect role selected. Please try again.");
                    return null;
                }
            }
        }
        System.out.println("Invalid username or password. Please try again.");
        return null;
    }

    @SuppressWarnings("unchecked")
    public void loadUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("users.dat"))) {
            users.addAll((List<User>) ois.readObject());
            System.out.println("Users loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No saved user data found.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading users.");
        }
    }

    public void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users);
            System.out.println("Users saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }
}

