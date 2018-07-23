/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.royale.compiler.internal.embedding.transcoders;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import org.apache.royale.compiler.common.ISourceLocation;
import org.apache.royale.compiler.config.CompilerDiagnosticsConstants;
import org.apache.royale.compiler.internal.embedding.EmbedData;
import org.apache.royale.compiler.internal.workspaces.Workspace;
import org.apache.royale.compiler.problems.EmbedCouldNotDetermineSampleFrameCountProblem;
import org.apache.royale.compiler.problems.EmbedUnsupportedSamplingRateProblem;
import org.apache.royale.compiler.problems.ICompilerProblem;
import org.apache.royale.swf.tags.DefineSoundTag;
import org.apache.royale.swf.tags.ICharacterTag;
import org.apache.royale.swf.tags.ITag;
import org.apache.royale.utils.DAByteArrayOutputStream;

/**
 * Handle the embedding of sound
 */
public class SoundTranscoder extends TranscoderBase
{
    // sampling rate
    // [frequency_index][version_index]
    private static final int[][] mp3frequencies =
    {
        {11025, 0, 22050, 44100},
        {12000, 0, 24000, 48000},
        {8000, 0, 16000, 32000},
        {0, 0, 0, 0}
    };


    // bitrate
    // [bits][version,layer]
    private static final int[][] mp3bitrates =
    {
        {0, 0, 0, 0, 0},
        {32, 32, 32, 32, 8},
        {64, 48, 40, 48, 16},
        {96, 56, 48, 56, 24},
        {128, 64, 56, 64, 32},
        {160, 80, 64, 80, 40},
        {192, 96, 80, 96, 48},
        {224, 112, 96, 112, 56},
        {256, 128, 112, 128, 64},
        {288, 160, 128, 144, 80},
        {320, 192, 160, 160, 96},
        {352, 224, 192, 176, 112},
        {384, 256, 224, 192, 128},
        {416, 320, 256, 224, 144},
        {448, 384, 320, 256, 160},
        {-1, -1, -1, -1, -1}
    };
    
    private static final int[][] mp3bitrateIndices =
    {
        // reserved, layer III, layer II, layer I
        {-1, 4, 4, 3}, // version 2.5
        {-1, -1, -1, -1}, // reserved
        {-1, 4, 4, 3}, // version 2
        {-1, 2, 1, 0}  // version 1
    };

    /**
     * Constructor.
     * 
     * @param data The embedding data.
     * @param workspace The workspace.
     */
    public SoundTranscoder(EmbedData data, Workspace workspace)
    {
        super(data, workspace);
    }

    @Override
    public boolean analyze(ISourceLocation location, Collection<ICompilerProblem> problems)
    {
        boolean result = super.analyze(location, problems);
        baseClassQName = CORE_PACKAGE + ".SoundAsset";
        return result;
    }

    @Override
    protected Map<String, ICharacterTag> doTranscode(Collection<ITag> tags, Collection<ICompilerProblem> problems)
    {
        InputStream strm = getDataStream(problems);        
        if (strm == null)
            return null;

        DefineSoundTag assetTag = buildSound(strm, problems);
        if (assetTag == null)
            return null;

        Map<String, ICharacterTag> symbolTags = Collections.singletonMap(data.getQName(), (ICharacterTag)assetTag);
        return symbolTags;
    }

    private DefineSoundTag buildSound(InputStream strm, Collection<ICompilerProblem> problems)
    {
        byte[] soundData = readFully(strm);
        if (soundData == null)
            return null;

        DefineSoundTag tag = new DefineSoundTag();
        tag.setSoundData(soundData);

        tag.setSoundFormat(2); // MP3
        tag.setSoundSize(1); // always 16-bit for compressed formats

        /**
         * 0 - version 2.5
         * 1 - reserved
         * 2 - version 2
         * 3 - version 1
         */
        int version = soundData[3] >> 3 & 0x3;

        /**
         * 0 - reserved
         * 1 - layer III => 1152 samples
         * 2 - layer II  => 1152 samples
         * 3 - layer I   => 384  samples
         */
        int layer = soundData[3] >> 1 & 0x3;

        int samplingRate = soundData[4] >> 2 & 0x3;

        /**
         * 0 - stereo
         * 1 - joint stereo
         * 2 - dual channel
         * 3 - single channel
         */
        int channelMode = soundData[5] >> 6 & 0x3;

        int frequency = mp3frequencies[samplingRate][version];

        /**
         * 1 - 11kHz
         * 2 - 22kHz
         * 3 - 44kHz
         */
        int rate;
        switch (frequency)
        {
        case 11025:
            rate = 1;
            break;
        case 22050:
            rate = 2;
            break;
        case 44100:
            rate = 3;
            break;
        default:
            problems.add(new EmbedUnsupportedSamplingRateProblem(data, frequency));
            return null;
        }
        tag.setSoundRate(rate);

        /**
         * 0 - mono
         * 1 - stereo
         */
        tag.setSoundType(channelMode == 3 ? 0 : 1);

        /**
         * assume that the whole thing plays in one SWF frame
         *
         * sample count = number of MP3 frames * number of samples per MP3
         */
        long sampleCount = countFrames(soundData) * (layer == 3 ? 384 : 1152);
        tag.setSoundSampleCount(sampleCount);

        if (sampleCount < 0)
        {
            // frame count == -1, error!
            problems.add(new EmbedCouldNotDetermineSampleFrameCountProblem(data));
            return null;
        }

        return tag;
    }

    private byte[] readFully(InputStream inputStream)
    {
        BufferedInputStream in = new BufferedInputStream(inputStream);
        DAByteArrayOutputStream baos = new DAByteArrayOutputStream();

        // write 2 bytes - number of frames to skip...
        baos.write(0);
        baos.write(0);

        // look for the first 11-bit frame sync. skip everything before the frame sync
        int b, state = 0;

        // 3-state FSM
        try
        {
            while ((b = in.read()) != -1)
            {
                if (state == 0)
                {
                    if (b == 255)
                    {
                        state = 1;
                    }
                }
                else if (state == 1)
                {
                    if ((b >> 5 & 0x7) == 7)
                    {
                        baos.write(255);
                        baos.write(b);
                        state = 2;
                    }
                    else
                    {
                        state = 0;
                    }
                }
                else if (state == 2)
                {
                    baos.write(b);
                }
                else
                {
                    // assert false;
                }
            }
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("SoundTranscoder waiting for lock in readFully");
            byte[] bb = baos.getDirectByteArray();
        	if ((CompilerDiagnosticsConstants.diagnostics & CompilerDiagnosticsConstants.DA_BYTEARRAY) == CompilerDiagnosticsConstants.DA_BYTEARRAY)
        		System.out.println("SoundTranscoder waiting for lock in readFully");
        	return bb;
        }
        catch (IOException e)
        {
        }
        finally
        {
            IOUtils.closeQuietly(baos);
            
            try
            {
                inputStream.close();
            }
            catch (IOException e)
            {
                // error case, don't do anything
            }
        }
        
        return new byte[0];
    }
    
    private int countFrames(byte[] bytes)
    {
        int count = 0, start = 2, b1, b2, b3;
        boolean skipped = false;

        while (start + 2 < bytes.length)
        {
            b1 = bytes[start] & 0xff;
            b2 = bytes[start + 1] & 0xff;
            b3 = bytes[start + 2] & 0xff;

            // check frame sync
            if (b1 != 255 || (b2 >> 5 & 0x7) != 7)
            {
                if (!skipped && start > 0) // LAME has a bug where they do padding wrong sometimes
                {
                    b3 = b2;
                    b2 = b1;
                    b1 = bytes[start - 1] & 0xff;
                    if (b1 != 255 || (b2 >> 5 & 0x7) != 7)
                    {
                        ++start;
                        continue;
                    }
                    else
                    {
                        --start;
                    }
                }
                else
                {
                    ++start;
                    continue;
                }
            }

            /**
             * 0 - version 2.5 1 - reserved 2 - version 2 3 - version 1
             */
            int version = b2 >> 3 & 0x3;

            /**
             * 0 - reserved 1 - layer III => 1152 samples 2 - layer II => 1152
             * samples 3 - layer I => 384 samples
             */
            int layer = b2 >> 1 & 0x3;

            int bits = b3 >> 4 & 0xf;
            int bitrateIndex = mp3bitrateIndices[version][layer];
            int bitrate = bitrateIndex != -1 ? mp3bitrates[bits][bitrateIndex] * 1000 : -1;

            if (bitrate == -1)
            {
                skipped = true;
                ++start;
                continue;
            }

            int samplingRate = b3 >> 2 & 0x3;

            int frequency = mp3frequencies[samplingRate][version];

            if (frequency == 0)
            {
                skipped = true;
                ++start;
                continue;
            }

            int padding = b3 >> 1 & 0x1;

            int frameLength = layer == 3 ?
                    (12 * bitrate / frequency + padding) * 4 :
                    144 * bitrate / frequency + padding;

            if (frameLength == 0)
            {
                // just in case. if we don't check frameLength, we may end up running an infinite loop!
                break;
            }
            else
            {
                start += frameLength;
            }

            skipped = false;
            count += 1;
        }

        return count;
    }
}
