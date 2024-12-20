import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Task1 {

    // Work Dealing: ExecutorService
    public static class TaskDealing implements Callable<Integer> {
        private final int[][] matrix;
        private final int startRow, endRow;

        public TaskDealing(int[][] matrix, int startRow, int endRow) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        public Integer call() {
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == i + j + 2) {
                        System.out.println("[WD] Знайдено елемент " + matrix[i][j] + " з індексом: " + (i+1) + " " + (j+1));
                        return matrix[i][j];
                    }
                }
            }
            return null;  // Якщо такого елемента не знайдено
        }
    }

    // Work Stealing: Fork/Join Pool
    public static class TaskStealing extends RecursiveTask<List<Integer>> {
        private final int[][] matrix;
        private final int startRow, endRow;

        public TaskStealing(int[][] matrix, int startRow, int endRow) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
        }

        @Override
        protected List<Integer> compute() {
            List<Integer> results = new ArrayList<>();
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] == i + j + 2) {
                        System.out.println("[WS] Знайдено елемент з індексом: " + (i+1) + " " + (j+1));
                        results.add(matrix[i][j]);
                    }
                }
            }
            return results;  // Повертаємо список знайдених елементів
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);

        // Введення розмірів масиву та діапазону значень
        System.out.print("Введіть кількість рядків масиву: ");
        int rows = scanner.nextInt();
        System.out.print("Введіть кількість стовпців масиву: ");
        int cols = scanner.nextInt();
        System.out.print("Введіть мінімальне значення елементів: ");
        int minValue = scanner.nextInt();
        System.out.print("Введіть максимальне значення елементів: ");
        int maxValue = scanner.nextInt();

        int[][] matrix = new int[rows][cols];
        Random rand = new Random();

        // Заповнення масиву випадковими числами в заданому діапазоні
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = rand.nextInt((maxValue - minValue) + 1) + minValue;
            }
        }

        // Виведення згенерованого масиву
        System.out.println("Згенерований масив:");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(matrix[i][j] + "\t");
            }
            System.out.println();
        }

        // Виконання пошуку за допомогою Work Dealing
        long startTime = System.nanoTime();
        int numThreads = 4;  // Кількість потоків для Work Dealing
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int chunkSize = rows / numThreads;
        List<Future<Integer>> futures = new ArrayList<>();

        // Розподіл роботи серед потоків
        for (int i = 0; i < numThreads; i++) {
            int startRow = i * chunkSize;
            int endRow = (i == numThreads - 1) ? rows : (i + 1) * chunkSize;
            futures.add(executor.submit(new TaskDealing(matrix, startRow, endRow)));
        }

        Integer result = null;
        for (Future<Integer> future : futures) {
            Integer found = future.get();
            if (found != null) {
                result = found;
                break;
            }
        }

        long endTime = System.nanoTime();

        if (result != null) {
        } else {
            System.out.println("Елемент не знайдений за допомогою Work Dealing.");
        }

        System.out.println("Час виконання (Work Dealing): " + (endTime - startTime) + " наносекунд");

        // Виконання пошуку за допомогою Work Stealing
        startTime = System.nanoTime();
        ForkJoinPool pool = new ForkJoinPool();
        TaskStealing taskStealing = new TaskStealing(matrix, 0, rows);
        List<Integer> resultStealing = pool.invoke(taskStealing);
        endTime = System.nanoTime();

        if (resultStealing.isEmpty()) {
            System.out.println("Елемент не знайдений за допомогою Work Stealing.");
        } else {
            System.out.println("Знайдені елементи за допомогою Work Stealing: " + resultStealing);
        }

        System.out.println("Час виконання (Work Stealing): " + (endTime - startTime) + " наносекунд");

        executor.shutdown();
        scanner.close();
    }
}
