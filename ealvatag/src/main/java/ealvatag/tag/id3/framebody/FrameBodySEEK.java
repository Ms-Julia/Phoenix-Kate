/**
 * @author : Paul Taylor
 * @author : Eric Farng
 * <p>
 * Version @version:$Id$
 * <p>
 * MusicTag Copyright (C)2003,2004
 * <p>
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public  License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, you can get a copy from
 * http://www.opensource.org/licenses/lgpl-license.php or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 * <p>
 * Description:
 */
package ealvatag.tag.id3.framebody;

import java.nio.ByteBuffer;

import ealvatag.tag.InvalidTagException;
import ealvatag.tag.datatype.DataTypes;
import ealvatag.tag.datatype.NumberFixedLength;
import ealvatag.tag.id3.ID3v24Frames;
import okio.Buffer;


public class FrameBodySEEK extends AbstractID3v2FrameBody implements ID3v24FrameBody {
    /**
     * Creates a new FrameBodySEEK datatype.
     */
    public FrameBodySEEK() {
        //        this.setObject("Minimum Offset to Next Tag", new Integer(0));
    }

    /**
     * Creates a new FrameBodySEEK datatype.
     *
     * @param minOffsetToNextTag
     */
    public FrameBodySEEK(int minOffsetToNextTag) {
        setObjectValue(DataTypes.OBJ_OFFSET, minOffsetToNextTag);
    }

    public FrameBodySEEK(FrameBodySEEK body) {
        super(body);
    }

    /**
     * Creates a new FrameBodySEEK datatype.
     *
     * @param byteBuffer
     * @param frameSize
     * @throws InvalidTagException if unable to create framebody from buffer
     */
    public FrameBodySEEK(ByteBuffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    public FrameBodySEEK(Buffer byteBuffer, int frameSize) throws InvalidTagException {
        super(byteBuffer, frameSize);
    }

    /**
     * The ID3v2 frame identifier
     *
     * @return the ID3v2 frame identifier  for this frame type
     */
    public String getIdentifier() {
        return ID3v24Frames.FRAME_ID_SEEK;
    }

    /**
     *
     */
    protected void setupObjectList() {
        addDataType(new NumberFixedLength(DataTypes.OBJ_OFFSET, this, 4));
    }
}