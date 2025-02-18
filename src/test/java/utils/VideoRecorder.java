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

    public static void startRecording(WebDriver driver, String testName) {
        if (isRecording) {
            System.out.println("Video recording already in progress");
            return;
        }

        try {
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

            // Configure the recorder with minimal settings
            screenRecorder = new ScreenRecorder(
                gc,
                null, // Record entire screen
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 16,
                    FrameRateKey, Rational.valueOf(10),
                    QualityKey, 0.5f,
                    KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                    FrameRateKey, Rational.valueOf(30)),
                null,
                videoDir
            ) {
                @Override
                protected File createMovieFile(Format fileFormat) throws IOException {
                    String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
                    File videoFile = new File(movieFolder, safeTestName + ".avi");
                    System.out.println("Creating video file: " + videoFile.getAbsolutePath());
                    return videoFile;
                }
            };

            // Start recording
            screenRecorder.start();
            isRecording = true;
            System.out.println("Started video recording: " + testName);

        } catch (Exception e) {
            System.out.println("Failed to start video recording: " + e.getMessage());
            e.printStackTrace();
            // Reset state in case of failure
            screenRecorder = null;
            isRecording = false;
        }
    }

    public static void stopRecording(String testName) {
        if (!isRecording || screenRecorder == null) {
            System.out.println("No active video recording to stop");
            return;
        }

        try {
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            isRecording = false;
            screenRecorder.stop();
            System.out.println("Video recording stopped: " + testName);
            
            // Verify the video file
            File videoFile = new File("target/videos/" + safeTestName + ".avi");
            if (videoFile.exists() && videoFile.length() > 0) {
                System.out.println("Video file created successfully: " + videoFile.getAbsolutePath() + 
                                 " (Size: " + videoFile.length() + " bytes)");
            } else {
                System.out.println("Warning: Video file is missing or empty: " + videoFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.out.println("Failed to stop video recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
        }
    }
} 