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

            // Configure the recorder
            screenRecorder = new ScreenRecorder(
                gc,
                screenBounds,  // Record full screen
                new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI),
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
                    File aviFile = new File(movieFolder, safeTestName + "_temp.avi");
                    currentVideoPath = aviFile.getAbsolutePath();
                    System.out.println("[VideoRecorder] Creating temporary AVI file: " + currentVideoPath);
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
            
            // Convert AVI to MP4
            File aviFile = new File(currentVideoPath);
            File mp4File = new File("target/videos/" + safeTestName + ".mp4");
            
            System.out.println("[VideoRecorder] Checking temporary AVI file: " + aviFile.getAbsolutePath());
            System.out.println("[VideoRecorder] AVI file exists: " + aviFile.exists());
            if (aviFile.exists()) {
                System.out.println("[VideoRecorder] AVI file size: " + aviFile.length() + " bytes");
            }
            
            if (aviFile.exists() && aviFile.length() > 0) {
                try {
                    System.out.println("[VideoRecorder] Converting AVI to MP4...");
                    
                    // Use FFmpeg to convert AVI to MP4 with better settings
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", aviFile.getAbsolutePath(),
                        "-c:v", "libx264",
                        "-crf", "23",  // Better quality
                        "-preset", "veryfast",  // Faster encoding
                        "-movflags", "+faststart",  // Better streaming
                        "-pix_fmt", "yuv420p",
                        "-y",  // Overwrite output file if it exists
                        mp4File.getAbsolutePath()
                    );
                    
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    
                    // Read and log FFmpeg output
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("[FFmpeg] " + line);
                        }
                    }
                    
                    // Wait for conversion to complete
                    int exitCode = process.waitFor();
                    System.out.println("[VideoRecorder] FFmpeg conversion exit code: " + exitCode);
                    
                    // Verify conversion result
                    if (mp4File.exists() && mp4File.length() > 0) {
                        aviFile.delete();
                        System.out.println("[VideoRecorder] Video converted successfully: " + mp4File.getAbsolutePath() + 
                                         " (Size: " + mp4File.length() + " bytes)");
                    } else {
                        System.out.println("[VideoRecorder] Failed to convert video. MP4 file not created or empty.");
                        // Try to preserve the AVI file if MP4 conversion fails
                        File fallbackFile = new File("target/videos/" + safeTestName + ".avi");
                        if (aviFile.renameTo(fallbackFile)) {
                            System.out.println("[VideoRecorder] Preserved original AVI file: " + fallbackFile.getAbsolutePath());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[VideoRecorder] Failed to convert video: " + e.getMessage());
                    e.printStackTrace();
                    // Try to preserve the AVI file if conversion fails
                    File fallbackFile = new File("target/videos/" + safeTestName + ".avi");
                    if (aviFile.renameTo(fallbackFile)) {
                        System.out.println("[VideoRecorder] Preserved original AVI file: " + fallbackFile.getAbsolutePath());
                    }
                }
            } else {
                System.out.println("[VideoRecorder] No temporary AVI file found or file is empty");
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