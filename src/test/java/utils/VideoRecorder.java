package utils;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class VideoRecorder {
    private static ScreenRecorder screenRecorder;
    private static String currentVideoPath;
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

            // Create and configure screen recorder
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            currentVideoPath = new File(videoDir, safeTestName + ".mp4").getAbsolutePath();

            screenRecorder = new ScreenRecorder(gc, captureSize,
                fileFormat, screenFormat, null, null,
                new File(videoDir));

            // Start recording
            screenRecorder.start();
            isRecording = true;

            System.out.println("[VideoRecorder] Started recording to: " + currentVideoPath);

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            screenRecorder = null;
            isRecording = false;
            currentVideoPath = null;
        }
    }

    public static void stopRecording(String testName) {
        if (!isRecording || screenRecorder == null) {
            System.out.println("[VideoRecorder] No active recording to stop");
            return;
        }

        try {
            // Stop recording
            screenRecorder.stop();
            
            // Get the recorded file
            File videoFile = new File(currentVideoPath);
            if (videoFile.exists() && videoFile.length() > 0) {
                System.out.println("[VideoRecorder] Recording saved successfully: " + currentVideoPath +
                                 " (Size: " + videoFile.length() + " bytes)");
            } else {
                System.out.println("[VideoRecorder] Warning: Video file is empty or does not exist: " + currentVideoPath);
            }

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Error during video recording cleanup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRecording = false;
            screenRecorder = null;
        }
    }
}