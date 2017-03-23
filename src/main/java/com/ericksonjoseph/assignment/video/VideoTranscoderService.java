
package com.ericksonjoseph.assignment.video;

import com.ericksonjoseph.assignment.config.Config;
import org.springframework.stereotype.Service;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.FFmpegExecutor;
import java.io.IOException;
import java.util.Arrays;

@Service
public class VideoTranscoderService {

    public void transcode(String filepath) throws VideoTranscoderException {

        try {

            FFmpeg ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            String output = filepath + Config.get("app.video.transcoder.output.suffix") + ".webm";

            executor.createJob(getWebMJob(filepath, output)).run();

        } catch (IOException e) {
            throw new VideoTranscoderException("Unable to transcode the given video. " + e.getMessage());
        }
    }

    /**
     * Creates a WebM transcoding Job
     */
    private FFmpegBuilder getWebMJob(String filepath, String destination) {
        return new FFmpegBuilder()
            .setInput(filepath)
            .overrideOutputFiles(true)
            .addOutput(destination)
            .setFormat("webm")
            .setVideoCodec("libvpx-vp9")
            .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
            .done();
    }
}
