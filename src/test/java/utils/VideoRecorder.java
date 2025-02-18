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
    private static boolean isRecording = false;
    private static String currentVideoPath;

    public static void startRecording(WebDriver driver, String testName) {
        if (isRecording) {
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
            System.out.println("[VideoRecorder] Video directory: " + videoDir.getAbsolutePath());

            // Get screen configuration
            GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
            
            Rectangle screenBounds = gc.getBounds();
            System.out.println("[VideoRecorder] Screen dimensions: " + screenBounds.width + "x" + screenBounds.height);

            // Configure the recorder
            screenRecorder = new ScreenRecorder(
                gc,
                screenBounds,  // Record full screen
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, "video/mp4"),
                new Format(MediaTypeKey, MediaType.VIDEO, 
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24,
                    FrameRateKey, Rational.valueOf(20),
                    QualityKey, 1.0f,
                    KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, 
                    EncodingKey, "black",
                    FrameRateKey, Rational.valueOf(30)),
                null,
                videoDir
            ) {
                @Override
                protected File createMovieFile(Format fileFormat) throws IOException {
                    String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
                    File mp4File = new File(movieFolder, safeTestName + ".mp4");
                    currentVideoPath = mp4File.getAbsolutePath();
                    System.out.println("[VideoRecorder] Creating video file: " + currentVideoPath);
                    return mp4File;
                }
            };

            // Start recording
            screenRecorder.start();
            isRecording = true;
            System.out.println("[VideoRecorder] Recording started successfully");

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
            System.out.println("[VideoRecorder] Stopping video recording for test: " + testName);
            
            isRecording = false;
            screenRecorder.stop();
            
            // Verify the video file
            File videoFile = new File(currentVideoPath);
            if (videoFile.exists() && videoFile.length() > 0) {
                System.out.println("[VideoRecorder] Video saved successfully: " + videoFile.getAbsolutePath() + 
                                 " (Size: " + videoFile.length() + " bytes)");
            } else {
                System.out.println("[VideoRecorder] Warning: Video file not found or empty: " + currentVideoPath);
            }

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to stop recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
            currentVideoPath = null;
        }
    }
} 