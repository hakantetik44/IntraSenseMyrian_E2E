package utils;

import org.monte.media.Format;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;
import org.openqa.selenium.WebDriver;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

            // Configure the recorder with improved settings
            screenRecorder = new ScreenRecorder(
                gc,
                screenBounds,
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, 
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, 24,
                    FrameRateKey, Rational.valueOf(15),  // Reduced frame rate for better stability
                    QualityKey, 0.8f,  // Slightly reduced quality for better performance
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
                    File aviFile = new File(movieFolder, safeTestName + ".avi");
                    currentVideoPath = aviFile.getAbsolutePath();
                    System.out.println("[VideoRecorder] Creating AVI file: " + currentVideoPath);
                    return aviFile;
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
            
            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            isRecording = false;
            screenRecorder.stop();

            // Get the source AVI file
            File sourceFile = new File(currentVideoPath);
            if (!sourceFile.exists() || sourceFile.length() == 0) {
                System.out.println("[VideoRecorder] Warning: Source video file is empty or does not exist: " + currentVideoPath);
                return;
            }

            // Convert AVI to MP4 using FFmpeg
            File targetFile = new File("target/videos/" + safeTestName + ".mp4");
            String ffmpegCommand = String.format("ffmpeg -i %s -c:v libx264 -preset ultrafast -crf 23 %s",
                    sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());

            Process process = Runtime.getRuntime().exec(ffmpegCommand);
            
            // Log FFmpeg output for debugging
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("[VideoRecorder] Successfully converted video to MP4: " + targetFile.getAbsolutePath());
                // Delete the original AVI file after successful conversion
                if (sourceFile.delete()) {
                    System.out.println("[VideoRecorder] Deleted original AVI file");
                }
            } else {
                System.out.println("[VideoRecorder] Failed to convert video. FFmpeg exit code: " + exitCode);
            }

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Error during video recording cleanup: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
            currentVideoPath = null;
        }
    }
}