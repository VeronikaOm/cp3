import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class Task2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Отримання директорії від користувача
        System.out.print("Введіть шлях до директорії: ");
        String directoryPath = scanner.nextLine();

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Вказаний шлях не є директорією або не існує.");
            return;
        }

        // Виконання пошуку зображень через Fork/Join Framework
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ImageSearchTask task = new ImageSearchTask(directory);
        List<File> imageFiles = forkJoinPool.invoke(task);

        // Результати
        System.out.println("Знайдено зображень: " + imageFiles.size());
        if (!imageFiles.isEmpty()) {
            File lastImage = imageFiles.get(imageFiles.size() - 1);
            System.out.println("Останній файл: " + lastImage.getAbsolutePath());

            // Спроба відкрити останній файл
            try {
                Desktop.getDesktop().open(lastImage);
            } catch (IOException e) {
                System.out.println("Не вдалося відкрити файл: " + e.getMessage());
            }
        }
    }

    // Клас для рекурсивного пошуку зображень
    static class ImageSearchTask extends RecursiveTask<List<File>> {
        private final File directory;
        private static final String[] IMAGE_EXTENSIONS = {".png", ".jpg", ".jpeg", ".bmp", ".gif"};

        public ImageSearchTask(File directory) {
            this.directory = directory;
        }

        @Override
        protected List<File> compute() {
            List<File> imageFiles = new ArrayList<>();
            List<ImageSearchTask> subTasks = new ArrayList<>();

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Рекурсивно створюємо підзадачі для підкаталогів
                        ImageSearchTask subTask = new ImageSearchTask(file);
                        subTasks.add(subTask);
                        subTask.fork();
                    } else if (isImageFile(file)) {
                        imageFiles.add(file);
                    }
                }

                for (ImageSearchTask subTask : subTasks) {
                    imageFiles.addAll(subTask.join());
                }
            }

            return imageFiles;
        }

        private boolean isImageFile(File file) {
            String name = file.getName().toLowerCase();
            for (String extension : IMAGE_EXTENSIONS) {
                if (name.endsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }
}

