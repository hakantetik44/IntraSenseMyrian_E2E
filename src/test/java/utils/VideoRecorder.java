package utils;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder {
    private static ScreenRecorder screenRecorder;
    private static File currentVideoFile;
    private static boolean isRecording = false;

    public static void startRecording(WebDriver driver, String testName) {
        if (isRecording) {
            System.out.println("[VideoRecorder] Recording already in progress");
            return;
        }

        try {
            // Create videos directory if it doesn't exist
            File videoDir = new File("target/videos");
            if (!videoDir.exists() && !videoDir.mkdirs()) {
                throw new IOException("Failed to create video directory");
            }

            // Get screen dimensions
            GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

            // Configure recording format
            Format fileFormat = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
            Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey,
                ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                QualityKey, 1.0f,
                KeyFrameIntervalKey, 15 * 60);

            // Configure recording dimensions
            Rectangle captureSize = new Rectangle(0, 0, 1920, 1080);

            // Create unique filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            currentVideoFile = new File(videoDir, safeTestName + "_" + timestamp + ".avi");

            // Create custom ScreenRecorder
            screenRecorder = new ScreenRecorder(gc, captureSize,
                fileFormat, screenFormat, null, null,
                currentVideoFile);

            // Start recording
            screenRecorder.start();
            isRecording = true;

            System.out.println("[VideoRecorder] Started recording to: " + currentVideoFile.getAbsolutePath());

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            screenRecorder = null;
            currentVideoFile = null;
            isRecording = false;
        }
    }

    public static File stopRecording() {
        if (!isRecording || screenRecorder == null) {
            System.out.println("[VideoRecorder] No active recording to stop");
            return null;
        }

        try {
            // Stop recording
            screenRecorder.stop();
            
            if (currentVideoFile != null && currentVideoFile.exists() && currentVideoFile.length() > 0) {
                System.out.println("[VideoRecorder] Recording saved successfully: " + currentVideoFile.getAbsolutePath() +
                                 " (Size: " + currentVideoFile.length() + " bytes)");
                return currentVideoFile;
            } else {
                System.out.println("[VideoRecorder] Warning: Video file is empty or does not exist");
                return null;
            }

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Error during video recording cleanup: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            isRecording = false;
            screenRecorder = null;
        }
    }
}