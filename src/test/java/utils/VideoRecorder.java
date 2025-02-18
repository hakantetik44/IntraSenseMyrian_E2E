package utils;

import org.openqa.selenium.WebDriver;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoRecorder {
    private static Robot robot;
    private static ScheduledExecutorService scheduler;
    private static List<File> frames;
    private static boolean isRecording = false;
    private static final int FRAME_RATE = 20; // frames per second

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void startRecording(WebDriver driver, String testName) {
        if (isRecording || robot == null) {
            return;
        }

        try {
            // Create directories
            File videoDir = new File("target/videos");
            File framesDir = new File("target/frames");
            videoDir.mkdirs();
            framesDir.mkdirs();

            // Initialize frame list
            frames = new ArrayList<>();

            // Start capturing frames
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                    BufferedImage capture = robot.createScreenCapture(screenRect);
                    File frame = new File(framesDir, System.currentTimeMillis() + ".png");
                    ImageIO.write(capture, "png", frame);
                    frames.add(frame);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 1000 / FRAME_RATE, TimeUnit.MILLISECONDS);

            isRecording = true;
            System.out.println("Started video recording: " + testName);

        } catch (Exception e) {
            System.out.println("Failed to start video recording: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopRecording(String testName) {
        if (!isRecording || scheduler == null) {
            return;
        }

        try {
            // Stop frame capture
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
            isRecording = false;

            // Create video from frames
            if (!frames.isEmpty()) {
                ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-framerate", String.valueOf(FRAME_RATE),
                    "-pattern_type", "glob",
                    "-i", "target/frames/*.png",
                    "-c:v", "libx264",
                    "-pix_fmt", "yuv420p",
                    "-preset", "ultrafast",
                    "-crf", "23",
                    "target/videos/" + testName + ".mp4"
                );

                Process process = processBuilder.start();
                process.waitFor();

                // Clean up frames
                for (File frame : frames) {
                    frame.delete();
                }
                new File("target/frames").delete();

                System.out.println("Video saved: target/videos/" + testName + ".mp4");
            }

        } catch (Exception e) {
            System.out.println("Failed to stop video recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scheduler = null;
            frames = null;
        }
    }
} 