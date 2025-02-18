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
            return;
        }

        try {
            // Create videos directory
            File videoDir = new File("target/videos");
            videoDir.mkdirs();

            // Get screen dimensions
            GraphicsConfiguration gc = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

            // Configure the recorder
            screenRecorder = new ScreenRecorder(
                gc,
                new Rectangle(0, 0, 1920, 1080),
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24, FrameRateKey, Rational.valueOf(15),
                    QualityKey, 1.0f,
                    KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black",
                    FrameRateKey, Rational.valueOf(30)),
                null,
                new File("target/videos")
            ) {
                @Override
                protected File createMovieFile(Format fileFormat) throws IOException {
                    return new File(movieFolder, testName.replaceAll("\\s+", "_") + ".avi");
                }
            };

            screenRecorder.start();
            isRecording = true;
            System.out.println("Started video recording: " + testName);

        } catch (Exception e) {
            System.out.println("Failed to start video recording: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stopRecording(String testName) {
        if (!isRecording || screenRecorder == null) {
            return;
        }

        try {
            screenRecorder.stop();
            isRecording = false;
            System.out.println("Video recording stopped: " + testName);
        } catch (Exception e) {
            System.out.println("Failed to stop video recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
        }
    }
} 