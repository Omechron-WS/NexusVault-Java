/*******************************************************************************
 * Copyright (C) 2018-2022 MarbleBag
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 *
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *******************************************************************************/

package nexusvault.format.tex;

import java.util.ArrayList;
import java.util.List;

public final class Image {

	public static enum ImageFormat {
		/** 3 bytes per pixel */
		RGB(3),
		/** 4 bytes per pixel */
		ARGB(4),
		/** 1 bytes per pixel */
		GRAYSCALE(1);

		private final int bytePerPixel;

		private ImageFormat(int bytePerPixel) {
			this.bytePerPixel = bytePerPixel;
		}

		public int getBytesPerPixel() {
			return this.bytePerPixel;
		}
	}

	private final ImageFormat format;
	private final int width;
	private final int height;
	private final int depth;
	private final byte[] data;

	public Image(int width, int height, int depth, ImageFormat format, byte[] data) {
		if (width <= 0) {
			throw new IllegalArgumentException("'width' must be greater than zero");
		}
		if (height <= 0) {
			throw new IllegalArgumentException("'height' must be greater than zero");
		}
		if (depth <= 0) {
			throw new IllegalArgumentException("'depth' must be greater than zero");
		}
		if (format == null) {
			throw new IllegalArgumentException("'format' must not be null");
		}
		if (data == null) {
			throw new IllegalArgumentException("'data' must not be null");
		}

		this.width = width;
		this.height = height;
		this.depth = depth;
		this.format = format;
		this.data = data;

		final int bytesPerPixel = format.getBytesPerPixel();
		final int expectedBytes = width * height * depth * bytesPerPixel;
		if (data.length != expectedBytes) {
			throw new IllegalArgumentException(
					String.format("Image data does not fit an image of %dx%d of type %s. Expected number of bytes %d, actual number of bytes %d", width, height,
							format.name(), expectedBytes, data.length));
		}
	}

	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	public byte[] getData() {
		return this.data;
	}

	public ImageFormat getFormat() {
		return this.format;
	}
        
        public static void blitLayerToImage(Image source, int sourceLayer, Image dest, int targetLayer) {
            if(source.width != dest.width) {
                throw new IllegalArgumentException("Input image is not the same width as this image.");
            }
            if(source.height != dest.height) {
                throw new IllegalArgumentException("Input image is not the same height as this image.");
            }
            
            for(int y = 0; y < dest.height; ++y) {
                for(int x = 0; x < dest.width; ++x) {
                    dest.data[x + dest.width * (y + (dest.height * targetLayer))] = source.data[x + source.width * (y + (source.height * sourceLayer))];
                }
            }
        }
        
        public static void make3DImage(List<Image> source) {
            Image first = source.get(0);
            Image img = new Image(first.width, first.height, source.size(), first.format, new byte[first.data.length * source.size()]);
            for(int z = 0; z < source.size(); ++z) {
                blitLayerToImage(source.get(z), 1, img, z);
            }
        }
        
        public ArrayList<Image> split3DImage() {
            ArrayList<Image> images = new ArrayList<>();
            for(int z = 0; z < depth; ++z) {
                Image img = new Image(width, height, 1, format, new byte[data.length / depth]);
                blitLayerToImage(this, z, img, 1);
            }
            return images;
        }

}
