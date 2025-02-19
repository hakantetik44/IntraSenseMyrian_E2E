package utils;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.WebDriver;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder {
    private static CustomScreenRecorder screenRecorder;
    private static String currentVideoPath;

    private static class CustomScreenRecorder extends ScreenRecorder {
        private String fileName;

        public CustomScreenRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                                  Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder,
                                  String fileName) throws IOException, AWTException {
            super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
            this.fileName = fileName;
        }

        @Override
        protected File createMovieFile(Format fileFormat) throws IOException {
            if (!movieFolder.exists()) {
                movieFolder.mkdirs();
            }
            return new File(movieFolder, fileName);
        }
    }

    public static void startRecording(WebDriver driver, String testName) {
        if (screenRecorder != null) {
            System.out.println("[VideoRecorder] Recording already in progress");
            return;
        }

        try {
            System.out.println("[VideoRecorder] Starting video recording for test: " + testName);
            
            // Create videos directory
            File videoDir = new File("target/videos");
            if (!videoDir.exists() && !videoDir.mkdirs()) {
                throw new IOException("Failed to create video directory: " + videoDir.getAbsolutePath());
            }

            // Get screen configuration
            GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

            // Get screen dimensions
            Rectangle captureArea = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            
            // Prepare video file name
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            String videoFileName = safeTestName + ".mp4";
            currentVideoPath = new File(videoDir, videoFileName).getAbsolutePath();

            // Configure and start recording
            screenRecorder = new CustomScreenRecorder(
                gc,
                captureArea,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24,
                    FrameRateKey, Rational.valueOf(10),  // Lower frame rate for stability
                    QualityKey, 0.7f,    // Lower quality for smaller file size
                    KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO,
                    EncodingKey, "black",
                    FrameRateKey, Rational.valueOf(30)),
                null,
                videoDir,
                videoFileName
            );

            screenRecorder.start();
            System.out.println("[VideoRecorder] Recording started successfully to: " + currentVideoPath);

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            screenRecorder = null;
            currentVideoPath = null;
        }
    }

    public static void stopRecording(String testName) {
        if (screenRecorder == null) {
            System.out.println("[VideoRecorder] No active recording to stop");
            return;
        }

        try {
            System.out.println("[VideoRecorder] Stopping video recording...");
            screenRecorder.stop();
            
            // Verify the recording
            File videoFile = new File(currentVideoPath);
            if (videoFile.exists() && videoFile.length() > 0) {
                System.out.println("[VideoRecorder] Recording saved successfully: " + currentVideoPath +
                                 " (Size: " + videoFile.length() + " bytes)");
            } else {
                System.out.println("[VideoRecorder] Warning: Video file is empty or does not exist: " + currentVideoPath);
            }

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Error stopping recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
        }
    }

    public static String getCurrentVideoPath() {
        return currentVideoPath;
    }
}