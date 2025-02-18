package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideoRecorder {
    private static List<File> screenshots = new ArrayList<>();
    private static String screenshotFolder = "target/screenshots/";
    private static String videoFolder = "target/videos/";
    private static long startTime;
    private static boolean isRecording = false;
    private static Thread recordingThread;
    private static WebDriver driver;

    public static void startRecording(WebDriver webDriver, String testName) {
        driver = webDriver;
        startTime = System.currentTimeMillis();
        isRecording = true;
        screenshots.clear();

        // Klasörleri oluştur
        new File(screenshotFolder).mkdirs();
        new File(videoFolder).mkdirs();

        // Screenshot alma thread'ini başlat
        recordingThread = new Thread(() -> {
            while (isRecording) {
                try {
                    captureScreenshot();
                    Thread.sleep(200); // 5 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        recordingThread.start();
    }

    private static void captureScreenshot() {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File targetFile = new File(screenshotFolder + System.currentTimeMillis() + ".png");
            FileUtils.copyFile(screenshot, targetFile);
            screenshots.add(targetFile);
        } catch (Exception e) {
            System.out.println("Screenshot alınamadı: " + e.getMessage());
        }
    }

    public static void stopRecording(String testName) {
        try {
            // Kayıt işlemini durdur
            isRecording = false;
            
            // Recording thread'in durmasını bekle
            if (recordingThread != null && recordingThread.isAlive()) {
                recordingThread.join(5000); // En fazla 5 saniye bekle
            }

            // Screenshot'ları video dosyasına dönüştür
            if (!screenshots.isEmpty()) {
                String videoPath = "target/allure-results/" + testName + ".mp4";
                createVideoFromScreenshots(videoPath);
            }

            // Geçici dosyaları temizle
            cleanup();
        } catch (Exception e) {
            System.out.println("Video kaydı durdurulamadı: " + e.getMessage());
        } finally {
            // Değişkenleri sıfırla
            driver = null;
            recordingThread = null;
            isRecording = false;
        }
    }

    private static void createVideoFromScreenshots(String outputPath) {
        try {
            File outputFile = new File(outputPath);
            // Output klasörünü oluştur
            outputFile.getParentFile().mkdirs();

            // Screenshot'ların olduğundan emin ol
            File[] files = new File(screenshotFolder).listFiles((dir, name) -> name.endsWith(".png"));
            if (files == null || files.length == 0) {
                System.out.println("Video oluşturulamadı: Screenshot bulunamadı");
                return;
            }

            // FFmpeg komutunu oluştur
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-y"); // Varolan dosyanın üzerine yaz
            command.add("-framerate");
            command.add("5");
            command.add("-pattern_type");
            command.add("glob");
            command.add("-i");
            command.add(screenshotFolder + "*.png");
            command.add("-vf");
            command.add("scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:-1:-1:color=black");
            command.add("-c:v");
            command.add("libx264");
            command.add("-pix_fmt");
            command.add("yuv420p");
            command.add("-preset");
            command.add("ultrafast");
            command.add(outputPath);

            System.out.println("FFmpeg komutu çalıştırılıyor: " + String.join(" ", command));

            // FFmpeg'i çalıştır
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Çıktıyı oku
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("FFmpeg: " + line);
                }
            }

            // İşlemin tamamlanmasını bekle
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Video başarıyla oluşturuldu: " + outputPath);
            } else {
                System.out.println("Video oluşturma başarısız. Exit code: " + exitCode);
            }

        } catch (Exception e) {
            System.out.println("Video oluşturma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cleanup() {
        // Screenshot'ları temizle
        for (File screenshot : screenshots) {
            screenshot.delete();
        }
        screenshots.clear();
    }
} 