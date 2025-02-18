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
                    File aviFile = new File(movieFolder, safeTestName + "_temp.avi");
                    System.out.println("Creating temporary AVI file: " + aviFile.getAbsolutePath());
                    return aviFile;
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
            
            // Convert AVI to MP4
            File aviFile = new File("target/videos/" + safeTestName + "_temp.avi");
            File mp4File = new File("target/videos/" + safeTestName + ".mp4");
            
            if (aviFile.exists() && aviFile.length() > 0) {
                try {
                    // Use FFmpeg to convert AVI to MP4
                    ProcessBuilder processBuilder = new ProcessBuilder(
                        "ffmpeg",
                        "-i", aviFile.getAbsolutePath(),
                        "-c:v", "libx264",
                        "-preset", "ultrafast",
                        "-pix_fmt", "yuv420p",
                        mp4File.getAbsolutePath()
                    );
                    
                    Process process = processBuilder.start();
                    process.waitFor();
                    
                    // Delete temporary AVI file
                    if (mp4File.exists() && mp4File.length() > 0) {
                        aviFile.delete();
                        System.out.println("Video converted and saved: " + mp4File.getAbsolutePath() + 
                                         " (Size: " + mp4File.length() + " bytes)");
                    } else {
                        System.out.println("Failed to convert video to MP4");
                    }
                } catch (Exception e) {
                    System.out.println("Failed to convert video: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Temporary AVI file not found or empty: " + aviFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.out.println("Failed to stop video recording: " + e.getMessage());
            e.printStackTrace();
        } finally {
            screenRecorder = null;
        }
    }
} 