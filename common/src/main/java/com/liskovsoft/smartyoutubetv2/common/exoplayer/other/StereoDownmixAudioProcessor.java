package com.liskovsoft.smartyoutubetv2.common.exoplayer.other;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.BaseAudioProcessor;

import java.nio.ByteBuffer;

/**
 * Downmixes multichannel (5.1/7.1) 16-bit PCM to stereo.<br/>
 * Old platform mixers (e.g. Android 4.4) drop or heavily attenuate the center channel,
 * where the dialogue lives, so voice sounds quiet compared to background sounds.<br/>
 * Channel order: FL, FR, FC, LFE, BL, BR, [SL, SR].
 */
public class StereoDownmixAudioProcessor extends BaseAudioProcessor {
    private static final float CENTER_GAIN = 0.707f;
    private static final float SURROUND_GAIN = 0.5f;

    @Override
    public boolean configure(int sampleRateHz, int channelCount, @C.PcmEncoding int encoding) throws UnhandledFormatException {
        if (encoding != C.ENCODING_PCM_16BIT) {
            throw new UnhandledFormatException(sampleRateHz, channelCount, encoding);
        }
        return setInputFormat(sampleRateHz, channelCount, encoding);
    }

    @Override
    public boolean isActive() {
        return channelCount == 6 || channelCount == 8;
    }

    @Override
    public int getOutputChannelCount() {
        return isActive() ? 2 : channelCount;
    }

    @Override
    public void queueInput(ByteBuffer inputBuffer) {
        int position = inputBuffer.position();
        int limit = inputBuffer.limit();
        int frameSize = channelCount * 2;
        int frameCount = (limit - position) / frameSize;
        ByteBuffer buffer = replaceOutputBuffer(frameCount * 4);

        for (int i = 0; i < frameCount; i++) {
            int base = position + i * frameSize;
            float fl = inputBuffer.getShort(base);
            float fr = inputBuffer.getShort(base + 2);
            float fc = inputBuffer.getShort(base + 4);
            // LFE (base + 6) is skipped, as in the standard ITU downmix
            float bl = inputBuffer.getShort(base + 8);
            float br = inputBuffer.getShort(base + 10);
            if (channelCount == 8) {
                bl += inputBuffer.getShort(base + 12);
                br += inputBuffer.getShort(base + 14);
            }

            float left = fl + CENTER_GAIN * fc + SURROUND_GAIN * bl;
            float right = fr + CENTER_GAIN * fc + SURROUND_GAIN * br;

            buffer.putShort(clip(left));
            buffer.putShort(clip(right));
        }

        inputBuffer.position(limit);
        buffer.flip();
    }

    private static short clip(float sample) {
        return (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
    }
}
