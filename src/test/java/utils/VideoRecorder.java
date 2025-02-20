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
    private static CustomScreenRecorder screenRecorder;
    private static boolean isRecording = false;

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
            } else if (!movieFolder.isDirectory()) {
                throw new IOException("\"" + movieFolder + "\" is not a directory.");
            }
            return new File(movieFolder, fileName);
        }
    }

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
            Rectangle captureSize = new Rectangle(0, 0, 1280, 720);

            // Create unique filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            String videoFileName = safeTestName + "_" + timestamp + ".avi";

            // Create custom ScreenRecorder
            screenRecorder = new CustomScreenRecorder(gc, captureSize,
                fileFormat, screenFormat, null, null,
                videoDir, videoFileName);

            // Start recording
            screenRecorder.start();
            isRecording = true;

            System.out.println("[VideoRecorder] Started recording to: " + new File(videoDir, videoFileName).getAbsolutePath());

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            screenRecorder = null;
            isRecording = false;
        }
    }

    public static File stopRecording() {
        if (!isRecording || screenRecorder == null) {
            System.out.println("[VideoRecorder] No active recording to stop");
            return null;
        }

        File videoFile = null;
        try {
            // Stop recording
            screenRecorder.stop();
            
            // Get the recorded file
            videoFile = screenRecorder.getCreatedMovieFiles().get(0);
            
            if (videoFile != null && videoFile.exists() && videoFile.length() > 0) {
                System.out.println("[VideoRecorder] Recording saved successfully: " + videoFile.getAbsolutePath() +
                                 " (Size: " + videoFile.length() + " bytes)");
                return videoFile;
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