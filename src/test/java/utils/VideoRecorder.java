package utils;

import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VideoRecorder {
    private static Process recordingProcess;
    private static String currentVideoPath;
    private static boolean isRecording = false;

    public static void startRecording(WebDriver driver, String testName) {
        if (isRecording) {
            System.out.println("[VideoRecorder] Recording already in progress");
            return;
        }

        try {
            // Create videos directory
            File videoDir = new File("target/videos");
            if (!videoDir.exists() && !videoDir.mkdirs()) {
                throw new IOException("Failed to create video directory: " + videoDir.getAbsolutePath());
            }

            String safeTestName = testName.replaceAll("[^a-zA-Z0-9-_]", "_");
            currentVideoPath = videoDir.getAbsolutePath() + File.separator + safeTestName + ".mp4";

            // FFmpeg command to record screen with hardware acceleration
            String[] command = {
                "ffmpeg",
                "-f", "avfoundation",  // macOS screen capture
                "-i", "1:none",        // screen device index (usually 1 for screen, none for no audio)
                "-framerate", "30",     // 30 fps
                "-video_size", "1920x1080", // adjust according to your screen resolution
                "-c:v", "h264_videotoolbox", // macOS hardware acceleration
                "-b:v", "2M",          // video bitrate
                "-pix_fmt", "yuv420p", // pixel format for better compatibility
                currentVideoPath
            };

            // Start recording
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            recordingProcess = processBuilder.start();

            // Start a thread to log FFmpeg output
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(recordingProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[FFmpeg] " + line);
                    }
                } catch (IOException e) {
                    System.out.println("[VideoRecorder] Error reading FFmpeg output: " + e.getMessage());
                }
            }).start();

            isRecording = true;
            System.out.println("[VideoRecorder] Started recording to: " + currentVideoPath);

        } catch (Exception e) {
            System.out.println("[VideoRecorder] Failed to start recording: " + e.getMessage());
            e.printStackTrace();
            recordingProcess = null;
            isRecording = false;
            currentVideoPath = null;
        }
    }

    public static void stopRecording(String testName) {
        if (!isRecording || recordingProcess == null) {
            System.out.println("[VideoRecorder] No active recording to stop");
            return;
        }

        try {
            // Send 'q' command to FFmpeg to stop recording gracefully
            Process quitProcess = Runtime.getRuntime().exec("pkill -INT -f ffmpeg");
            quitProcess.waitFor();

            // Wait for recording process to finish
            recordingProcess.waitFor();

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
            recordingProcess = null;
        }
    }
}