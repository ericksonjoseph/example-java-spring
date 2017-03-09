
package com.ericksonjoseph.assignment.video;

import com.ericksonjoseph.assignment.config.Config;
import org.springframework.stereotype.Service;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import java.io.IOException;
import java.util.Arrays;

@Service
public class VideoValidationService {

	// No limit by default
	private float maxDuration = 0;

	// No format required by default
	private String requiredFormat = null;

	public VideoValidationService setMaxDuration(float duration) {
		this.maxDuration = duration;
		return this;
	}

	public VideoValidationService setFormat(String requiredFormat) {
		this.requiredFormat = requiredFormat;
		return this;
	}

	public String validate(String file) throws VideoValidationException {

		String data = "";
		FFmpegProbeResult probeResult = null;

		// Try to ffprobe the video
		try {
			FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");
			probeResult = ffprobe.probe(file);
		} catch (IOException e) {
			throw new VideoValidationException("Unable to validate the file. " + e.getMessage());
		}

		// Collect data about the video
		FFmpegFormat format = probeResult.getFormat();
		data += String.format("%nFile: %s%nFormat: %s%nBitrate: %s%nDuration: %.2fs", 
				format.filename, 
				format.format_long_name,
				format.bit_rate,
				format.duration
				);

		FFmpegStream stream = probeResult.getStreams().get(0);
		data += String.format("%nCodec: '%s' Width: %dpx Height: %dpx",
				stream.codec_long_name,
				stream.width,
				stream.height
				);

		checkRequirements(format);

		return data;
	}

	private void checkRequirements(FFmpegFormat format) throws VideoValidationException {

		if (this.maxDuration != 0 && format.duration > this.maxDuration) {
			throw new VideoValidationException("File exceeds maximum of " + this.maxDuration + " seconds");
		}

		if (this.requiredFormat != null) {
			String[] formats = format.format_name.split(",");
			if (!Arrays.asList(formats).contains(this.requiredFormat)) {
				throw new VideoValidationException("Incorrect video format. " + this.requiredFormat + " required");
			}
		}
	}
}
