/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.storage.base;

import java.util.Map;
import java.util.Locale;
import java.io.IOException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import static java.lang.Math.addExact;
import static java.lang.Math.subtractExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.multiplyFull;
import static java.lang.Math.incrementExact;
import static java.lang.Math.decrementExact;
import static java.lang.Math.toIntExact;
import static java.lang.Math.floorDiv;
import org.opengis.util.GenericName;
import org.opengis.metadata.spatial.DimensionNameType;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.DisjointExtentException;
import org.apache.sis.coverage.privy.DeferredProperty;
import org.apache.sis.coverage.privy.TiledImage;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.storage.tiling.TileMatrixSet;
import org.apache.sis.storage.internal.Resources;
import org.apache.sis.util.collection.WeakValueHashMap;
import org.apache.sis.util.resources.Errors;
import static org.apache.sis.pending.jdk.JDK18.ceilDiv;

// Specific to the geoapi-3.1 and geoapi-4.0 branches:
import org.opengis.coverage.CannotEvaluateException;

// Specific to the geoapi-4.0 branch:
import org.opengis.coordinate.MismatchedDimensionException;


/**
 * Base class of grid coverage read from a resource where data are stored in tiles.
 * This grid coverage may represent only a subset of the coverage resource.
 * Tiles are read from the storage only when first needed.
 *
 * <h2>Cell coordinates</h2>
 * When there is no subsampling, this coverage uses the same cell coordinates as the originating resource.
 * When there is a subsampling, cell coordinates in this coverage are divided by the subsampling factors.
 * Conversions are done by {@link #pixelToResourceCoordinates(Rectangle)}.
 *
 * <p><b>DEsign note:</b> {@code TiledGridCoverage} use the same cell coordinates as the originating
 * {@link TiledGridResource} (when no subsampling) because those two classes use {@code long} integers.
 * There is no integer overflow to avoid.</p>
 *
 * <h2>Tile matrix coordinate (<abbr>TMC</abbr>)</h2>
 * In each {@code TiledGridCoverage}, indices of tiles starts at (0, 0, …).
 * This class does not use the same tile indices as the coverage resource in order to avoid integer overflow.
 * Each {@code TiledGridCoverage} instance uses its own, independent, Tile Matrix Coordinates (<abbr>TMC</abbr>).
 *
 * @author  Martin Desruisseaux (Geomatys)
 */
public abstract class TiledGridCoverage extends GridCoverage {
    /**
     * Number of dimensions in a rendered image.
     * Used for identifying codes where a two-dimensional slice is assumed.
     */
    protected static final int BIDIMENSIONAL = 2;

    /**
     * The dimensions of <var>x</var> and <var>y</var> axes.
     * Static constants for now, may become configurable fields in the future.
     */
    protected static final int X_DIMENSION = 0, Y_DIMENSION = 1;

    /**
     * The area to read in unit of the full coverage (without subsampling).
     * This is the intersection between user-specified domain and the source
     * {@link TiledGridResource} domain, expanded to an integer number of tiles.
     */
    private final GridExtent readExtent;

    /**
     * Whether to force the {@link #readExtent} tile intersection to the {@link #virtualTileSize}.
     * This is relevant only for the last column of tile matrix, because those tiles may be truncated
     * if the image size is not a multiple of tile size. It is usually necessary to read those tiles
     * fully anyway because otherwise, the pixels read from the storage would not be aligned with the
     * pixels stored in the {@link Raster}. However, there is a few exceptions where the read extent
     * should not be forced to the tile size:
     *
     * <ul>
     *   <li>If the image is untiled, then the {@link org.apache.sis.storage.base.TiledGridResource.Subset}
     *       constructor assumes that only the requested region of the tile will be read.</li>
     *   <li>If the tile is truncated on the storage as well
     *       (note: this is rare. GeoTIFF for example always stores whole tiles).</li>
     * </ul>
     *
     * <p>In current version this is a flag for the <var>x</var> dimension only. In a future version
     * it could be flags for other dimensions as well (using bitmask) if it appears to be useful.</p>
     */
    private final boolean forceTileSize;

    /**
     * Size of all tiles in the domain of this {@code TiledGridCoverage}, without clipping and subsampling.
     * All coverages created from the same {@link TiledGridResource} shall have the same tile size values.
     * The length of this array is the number of dimensions in the source {@link GridExtent}.
     * This is often {@value #BIDIMENSIONAL} but can also be more.
     *
     * <p>The tile size may be virtual if the {@link TiledGridResource} subclass decided to coalesce
     * many real tiles in bigger virtual tiles. This is sometime useful when a subsampling is applied,
     * for avoiding that the subsampled tiles become too small.
     * This strategy may be convenient when coalescing is easy.</p>
     *
     * @see #getTileSize(int)
     */
    private final long[] virtualTileSize;

    /**
     * Values by which to multiply each tile coordinates for obtaining the index in the tile vector.
     * The length of this array is the same as {@link #virtualTileSize}. All coverages created from
     * the same {@link TiledGridResource} have the same stride values.
     */
    private final int[] tileStrides;

    /**
     * Index of the first {@code TiledGridCoverage} tile in a row-major array of tiles.
     * This is the value to add to the index computed with {@link #tileStrides} before to access vector elements.
     */
    private final int indexOfFirstTile;

    /**
     * The Tile Matrix Coordinates (<abbr>TMC</abbr>) that tile (0,0) of this coverage
     * would have in the originating {@code TiledGridResource}.
     * This is the value to subtract from tile indices computed from pixel coordinates.
     *
     * <p>The current implementation assumes that the tile (0,0) in the resource starts
     * at cell coordinates (0,0) of the resource.</p>
     *
     * @see #indexOfFirstTile
     * @see AOI#getTileCoordinatesInResource()
     */
    private final long[] tmcOfFirstTile;

    /**
     * Conversion from pixel coordinates in this (potentially subsampled) coverage
     * to cell coordinates in the originating resource coverage at full resolution.
     * The conversion from (<var>x</var>, <var>y</var>) to (<var>x′</var>, <var>y′</var>) is as below,
     * where <var>s</var> are subsampling factors and <var>t</var> are subsampling offsets:
     *
     * <ul>
     *   <li><var>x′</var> = s₀⋅<var>x</var> + t₀</li>
     *   <li><var>y′</var> = s₁⋅<var>y</var> + t₁</li>
     * </ul>
     *
     * This transform maps {@linkplain org.apache.sis.coverage.grid.PixelInCell#CELL_CORNER pixel corners}.
     *
     * @see #getSubsampling(int)
     * @see #pixelToResourceCoordinate(long, int)
     */
    private final int[] subsampling, subsamplingOffsets;

    /**
     * Indices of {@link TiledGridResource} bands which have been retained for
     * inclusion in this {@code TiledGridCoverage}, in strictly increasing order.
     * An "included" band is stored in memory but not necessarily visible to the user,
     * because the {@link SampleModel} can be configured for ignoring some bands.
     * This array is {@code null} if all bands shall be included.
     *
     * <p>If the user specified bands out of order, the change of band order is taken in account
     * by the sample {@link #model}. This {@code includedBands} array does not apply any change
     * of order for making sequential readings easier.</p>
     */
    protected final int[] includedBands;

    /**
     * Cache of rasters read by this {@code TiledGridCoverage}. This cache may be shared with other coverages
     * created for the same {@link TiledGridResource} resource. For each value, the raster {@code minX} and
     * {@code minY} values can be anything, depending which {@code TiledGridCoverage} was first to load the tile.
     *
     * @see TiledGridResource#rasters
     * @see AOI#getCachedTile()
     * @see #createCacheKey(int)
     */
    private final WeakValueHashMap<TiledGridResource.CacheKey, Raster> rasters;

    /**
     * The sample model for all rasters. The width and height of this sample model are the two first elements
     * of {@link #virtualTileSize} divided by subsampling and clipped to the domain.
     * If user requested to read only a subset of the bands, then this sample model is already the subset.
     *
     * @see TiledGridResource#getSampleModel(int[])
     */
    protected final SampleModel model;

    /**
     * The Java2D color model for images rendered from this coverage.
     *
     * @see TiledGridResource#getColorModel(int[])
     */
    protected final ColorModel colors;

    /**
     * The values to use for filling empty spaces in rasters, or {@code null} if zero in all bands.
     * If non-null, the array length is equal to the number of bands.
     *
     * @see TiledGridResource#getFillValues(int[])
     */
    protected final Number[] fillValues;

    /**
     * Whether the reading of tiles is deferred to {@link RenderedImage#getTile(int, int)} time.
     */
    private final boolean deferredTileReading;

    /**
     * Creates a new tiled grid coverage.
     *
     * @param  subset  description of the {@link TiledGridResource} subset to cover.
     * @throws ArithmeticException if the number of tiles overflows 32 bits integer arithmetic.
     */
    protected TiledGridCoverage(final TiledGridResource.Subset subset) {
        super(subset.domain, subset.ranges);
        final GridExtent extent = subset.domain.getExtent();
        final int dimension = subset.sourceExtent.getDimension();
        deferredTileReading = subset.deferredTileReading();
        readExtent          = subset.readExtent;
        subsampling         = subset.subsampling;
        subsamplingOffsets  = subset.subsamplingOffsets;
        includedBands       = subset.includedBands;
        rasters             = subset.cache;
        virtualTileSize     = subset.virtualTileSize;
        tmcOfFirstTile      = new long[dimension];
        tileStrides         = new int [dimension];
        final int[] subSize = new int [dimension];

        @SuppressWarnings("LocalVariableHidesMemberVariable")
        long indexOfFirstTile = 0;
        int  tileStride       = 1;
        for (int i=0; i<dimension; i++) {
            final long ts     = virtualTileSize[i];
            tmcOfFirstTile[i] = floorDiv(readExtent.getLow(i), ts);
            tileStrides[i]    = tileStride;
            subSize[i]        = toIntExact(Math.min(((ts-1) / subsampling[i]) + 1, extent.getSize(i)));
            indexOfFirstTile  = addExact(indexOfFirstTile, multiplyExact(tmcOfFirstTile[i], tileStride));
            int tileCount     = toIntExact(ceilDiv(subset.sourceExtent.getSize(i), ts));
            tileStride        = multiplyExact(tileCount, tileStride);
        }
        this.indexOfFirstTile = toIntExact(indexOfFirstTile);
        /*
         * At this point, `tileStride` is the total number of tiles in source.
         * This value is not stored but its computation is still useful because
         * we want `ArithmeticException` to be thrown if the value is too high.
         */
        @SuppressWarnings("LocalVariableHidesMemberVariable")
        SampleModel model = subset.modelForBandSubset;
        if (model.getWidth() != subSize[X_DIMENSION] || model.getHeight() != subSize[Y_DIMENSION]) {
            model = model.createCompatibleSampleModel(subSize[X_DIMENSION], subSize[Y_DIMENSION]);
        }
        this.model      = model;
        this.colors     = subset.colorsForBandSubset;
        this.fillValues = subset.fillValues;
        forceTileSize   = multiplyFull(subSize[X_DIMENSION], subsampling[X_DIMENSION]) == virtualTileSize[X_DIMENSION];
    }

    /**
     * Returns a unique name that identifies this coverage.
     * The name shall be unique in the {@link TileMatrixSet}.
     *
     * @return an human-readable identification of this coverage.
     */
    protected abstract GenericName getIdentifier();

    /**
     * Returns the locale for error messages, or {@code null} for the default.
     *
     * @return the locale for warning or error messages, or {@code null} if unspecified.
     */
    protected Locale getLocale() {
        return null;
    }

    /**
     * Returns the size of all tiles in the domain of this {@code TiledGridCoverage}, without clipping and subsampling.
     * It may be a virtual tile size if the {@link TiledGridResource} subclass decided to coalesce many real tiles into
     * fewer bigger virtual tiles.
     *
     * @param  dimension  dimension for which to get tile size.
     * @return tile size in the given dimension, without clipping and subsampling.
     */
    protected final long getTileSize(final int dimension) {
        return virtualTileSize[dimension];
    }

    /**
     * Returns the subsampling in the given dimension.
     *
     * @param  dimension  dimension for which to get subsampling.
     * @return subsampling as a value ≥ 1.
     */
    protected final int getSubsampling(final int dimension) {
        return subsampling[dimension];
    }

    /**
     * Converts a cell coordinate from this coverage to the {@code TiledGridResource} coordinate space.
     * This method removes the subsampling effect, i.e. returns the coordinate that we would have if this
     * coverage was at full resolution. Such unsampled {@code TiledGridCoverage} uses the same coordinates
     * as the originating {@link TiledGridResource}.
     *
     * <p>This method uses the "pixel" word for simplicity and because this method is used mostly
     * for the first two dimensions, but "pixel" should be understood as "grid coverage cell".</p>
     *
     * @param  coordinate  coordinate in this {@code TiledGridCoverage} domain.
     * @param  dimension   dimension of the coordinate.
     * @return coordinate in this {@code TiledGridResource} with no subsampling applied.
     * @throws ArithmeticException if the coordinate cannot be represented as a long integer.
     *
     * @see #pixelToResourceCoordinates(Rectangle)
     */
    private long pixelToResourceCoordinate(final long coordinate, final int dimension) {
        return addExact(multiplyExact(coordinate, subsampling[dimension]), subsamplingOffsets[dimension]);
    }

    /**
     * Converts a cell coordinate from {@link TiledGridResource} space to {@code TiledGridCoverage} coordinate.
     * This is the converse of {@link #pixelToResourceCoordinate(long, int)}.
     * Note that there is a possible accuracy lost.
     *
     * @param  coordinate  coordinate in the {@code TiledGridResource} domain.
     * @param  dimension   dimension of the coordinate.
     * @return coordinates in this subsampled {@code TiledGridCoverage} domain.
     * @throws ArithmeticException if the coordinate cannot be represented as a long integer.
     */
    private long resourceToPixelCoordinate(final long coordinate, final int dimension) {
        return floorDiv(subtractExact(coordinate, subsamplingOffsets[dimension]), subsampling[dimension]);
    }

    /**
     * Converts a cell coordinate from this {@code TiledGridCoverage} coordinate space to
     * the Tile Matrix Coordinate (<abbr>TMC</abbr>) of the tile which contains that cell.
     * The <abbr>TMC</abbr> is relative to the full {@link TiledGridResource},
     * i.e. without subtraction of {@link #tmcOfFirstTile}.
     *
     * @param  coordinate  coordinates in this {@code TiledGridCoverage} domain.
     * @param  dimension   dimension of the coordinate.
     * @return Tile Matrix Coordinate (TMC) of the tile which contains the specified cell.
     * @throws ArithmeticException if the coordinate cannot be represented as an integer.
     */
    private long toResourceTileMatrixCoordinate(final long coordinate, final int dimension) {
        return floorDiv(pixelToResourceCoordinate(coordinate, dimension), virtualTileSize[dimension]);
    }

    /**
     * Returns the number of pixels in a single bank element. This is usually 1, except for
     * {@link MultiPixelPackedSampleModel} which packs many pixels in a single bank element.
     * This value is a power of 2 according {@code MultiPixelPackedSampleModel} specification.
     *
     * <h4>Design note</h4>
     * This is "pixels per element", not "samples per element". It makes a difference in the
     * {@link java.awt.image.SinglePixelPackedSampleModel} case, for which this method returns 1
     * (by contrast a "samples per element" would give a value greater than 1).
     * But this value can nevertheless be understood as a "samples per element" value
     * where only one band is considered at a time.
     *
     * @return number of pixels in a single bank element. Usually 1.
     *
     * @see SampleModel#getSampleSize(int)
     * @see MultiPixelPackedSampleModel#getPixelBitStride()
     */
    protected final int getPixelsPerElement() {
        return getPixelsPerElement(model);
    }

    /**
     * Implementation of {@link #getPixelsPerElement()}.
     *
     * @param  model  the sample model from which to infer the number of pixels per bank element.
     * @return number of pixels in a single bank element. Usually 1.
     */
    static int getPixelsPerElement(final SampleModel model) {
        if (model instanceof MultiPixelPackedSampleModel) {
            /*
             * The following code performs the same computation as `MultiPixelPackedSampleModel`
             * constructor when computing its package-private field `pixelsPerDataElement`.
             * That constructor ensured that `sampleSize` is a divisor of `typeSize`.
             */
            final int typeSize   = DataBuffer.getDataTypeSize(model.getDataType());
            final int sampleSize = ((MultiPixelPackedSampleModel) model).getPixelBitStride();
            final int pixelsPerElement = typeSize / sampleSize;
            if (pixelsPerElement > 0) {                         // Paranoiac check.
                return pixelsPerElement;
            }
        }
        return 1;
    }

    /**
     * Returns a two-dimensional slice of grid data as a rendered image.
     *
     * @param  sliceExtent  a subspace of this grid coverage, or {@code null} for the whole image.
     * @return the grid slice as a rendered image. Image location is relative to {@code sliceExtent}.
     */
    @Override
    public RenderedImage render(GridExtent sliceExtent) {
        final GridExtent available = gridGeometry.getExtent();
        final int dimension = available.getDimension();
        if (sliceExtent == null) {
            sliceExtent = available;
        } else {
            final int sd = sliceExtent.getDimension();
            if (sd != dimension) {
                throw new MismatchedDimensionException(Errors.format(
                        Errors.Keys.MismatchedDimension_3, "sliceExtent", dimension, sd));
            }
        }
        final int[] selectedDimensions = sliceExtent.getSubspaceDimensions(BIDIMENSIONAL);
        if (selectedDimensions[1] != 1) {
            // TODO
            throw new UnsupportedOperationException("Non-horizontal slices not yet implemented.");
        }
        final RenderedImage image;
        try {
            final int[] tileLower = new int[dimension];         // Indices of first tile to read, inclusive.
            final int[] tileUpper = new int[dimension];         // Indices of last tile to read, exclusive.
            final int[] offsetAOI = new int[dimension];         // Pixel offset compared to Area Of Interest.
            final int[] imageSize = new int[dimension];         // Subsampled image size.
            for (int i=0; i<dimension; i++) {
                final long min    = available  .getLow (i);     // Lowest valid coordinate in subsampled image.
                final long max    = available  .getHigh(i);     // Highest valid coordinate, inclusive.
                final long aoiMin = sliceExtent.getLow (i);     // Requested coordinate in subsampled image.
                final long aoiMax = sliceExtent.getHigh(i);
                final long tileUp = incrementExact(toResourceTileMatrixCoordinate(Math.min(aoiMax, max), i));
                final long tileLo =                toResourceTileMatrixCoordinate(Math.max(aoiMin, min), i);
                if (tileUp <= tileLo) {
                    final String message = Errors.forLocale(getLocale())
                            .getString(Errors.Keys.IllegalRange_2, aoiMin, aoiMax);
                    if (aoiMin > aoiMax) {
                        throw new IllegalArgumentException(message);
                    } else {
                        throw new DisjointExtentException(message);
                    }
                }
                // Lower and upper coordinates in subsampled image, rounded to integer number of tiles and clipped to available data.
                final long lower = /* inclusive */Math.max(resourceToPixelCoordinate(/* inclusive */multiplyExact(tileLo, virtualTileSize[i]),  i), min);
                final long upper = incrementExact(Math.min(resourceToPixelCoordinate(decrementExact(multiplyExact(tileUp, virtualTileSize[i])), i), max));
                imageSize[i] = toIntExact(subtractExact(upper, lower));
                offsetAOI[i] = toIntExact(subtractExact(lower, aoiMin));
                tileLower[i] = toIntExact(subtractExact(tileLo, tmcOfFirstTile[i]));
                tileUpper[i] = toIntExact(subtractExact(tileUp, tmcOfFirstTile[i]));
            }
            /*
             * Prepare an iterator over all tiles to read, together with the following properties:
             *    - Two-dimensional conversion from pixel coordinates to "real world" coordinates.
             */
            final var iterator = new TileIterator(tileLower, tileUpper, offsetAOI, dimension);
            final Map<String,Object> properties = DeferredProperty.forGridGeometry(gridGeometry, selectedDimensions);
            if (deferredTileReading) {
                image = new TiledDeferredImage(imageSize, tileLower, properties, iterator);
            } else {
                /*
                 * If the loading strategy is not `RasterLoadingStrategy.AT_GET_TILE_TIME`, get all tiles
                 * in the area of interest now. I/O operations, if needed, happen in `readTiles(…)` call.
                 */
                final Raster[] result = readTiles(iterator);
                image = new TiledImage(properties, colors,
                        imageSize[X_DIMENSION], imageSize[Y_DIMENSION],
                        tileLower[X_DIMENSION], tileLower[Y_DIMENSION], result);
            }
        } catch (Exception e) {     // Too many exception types for listing them all.
            throw new CannotEvaluateException(Resources.forLocale(getLocale()).getString(
                    Resources.Keys.CanNotRenderImage_1, getIdentifier().toFullyQualifiedName()), e);
        }
        return image;
    }




    /**
     * An Area Of Interest (<abbr>AOI</abbr>) describing a tile area or sub-area to read in response to a user's request.
     * {@code AOI} can be a mutable iterator over all the tiles to read ({@link TileIterator}) or an immutable snapshot
     * of the iterator position as an instant ({@link Snapshot}).
     */
    protected static abstract class AOI {
        /**
         * Tile Matrix Coordinates (TMC) relative to the enclosing {@link TiledGridCoverage}.
         * Tile (0,0) is the tile in the upper-left corner of this {@link TiledGridCoverage},
         * not necessarily the tile in the upper-left corner of the image in the resource.
         *
         * <p>In the case of {@link Snapshot}, this array shall be considered unmodifiable.
         * In the case of {@link TileIterator}, this array is initialized to a clone of
         * {@link #tileLower} and is modified by calls to {@link TileIterator#next()}.</p>
         */
        final int[] tmcInSubset;

        /**
         * Current iterator position as an index in the array of tiles to be returned by {@link #readTiles(TileIterator)}.
         * The initial position is zero. This field is incremented by calls to {@link TileIterator#next()}.
         *
         * @see #getTileIndexInResultArray()
         */
        int indexInResultArray;

        /**
         * Current iterator position as an index in the vector of tiles in the {@link TiledGridResource}.
         * Tiles are assumed stored in a row-major fashion. This field is incremented by calls to {@link #next()}.
         * This index is also used as key in the {@link TiledGridCoverage#rasters} map.
         *
         * <h4>Example</h4>
         * In a GeoTIFF image, this is the index of the tile in the {@code tileOffsets}
         * and {@code tileByteCounts} vectors.
         */
        int indexInTileVector;

        /**
         * Creates a new area of interest.
         */
        AOI(final int[] tmcInSubset) {
            this.tmcInSubset = tmcInSubset;
        }

        /**
         * Returns the enclosing coverage.
         */
        abstract TiledGridCoverage getCoverage();

        /**
         * Returns the current <abbr>AOI</abbr> position in the tile matrix of the original resource.
         * This method assumes that the upper-left corner of tile (0,0) in the resource starts at cell
         * coordinates (0,0) of the resource.
         *
         * @return current <abbr>AOI</abbr> tile coordinates in original coverage resource.
         */
        public final long[] getTileCoordinatesInResource() {
            final long[] tmcOfFirstTile = getCoverage().tmcOfFirstTile;
            final long[] coordinate = new long[tmcOfFirstTile.length];
            for (int i = 0; i < coordinate.length; i++) {
                coordinate[i] = addExact(tmcOfFirstTile[i], tmcInSubset[i]);
            }
            return coordinate;
        }

        /**
         * Returns the current <abbr>AOI</abbr> position as an index in the vector of tiles of the original resource.
         * Tiles are assumed stored in a row-major fashion. with the first tiles starting at index 0.
         *
         * @return current <abbr>AOI</abbr> tile index in original coverage resource.
         */
        public final int getTileIndexInResource() {
            return indexInTileVector;
        }

        /**
         * Returns the current <abbr>AOI</abbr> position as an index in the array of tiles to be returned
         * by {@code TiledGridCoverage.readTiles(…)}. If this <abbr>AOI</abbr> is an iterator, the initial
         * position is zero and is incremented by 1 in each call to {@link TileIterator#next()}.
         *
         * @return current <abbr>AOI</abbr> tile index in the result array of tiles.
         *
         * @see #readTiles(TileIterator)
         */
        public final int getTileIndexInResultArray() {
            return indexInResultArray;
        }

        /**
         * Returns the origin to assign to the tile at the current iterator position.
         * See {@link TileIterator#getTileOrigin(int)} for more explanation.
         *
         * @see TileIterator#getTileOrigin(int)
         */
        abstract int getTileOrigin(final int dimension);

        /**
         * Returns the cached tile for current <abbr>AOI</abbr> position.
         *
         * @return cached tile at current <abbr>AOI</abbr> position, or {@code null} if none.
         *
         * @see #cache(Raster)
         */
        public Raster getCachedTile() {
            final TiledGridCoverage coverage = getCoverage();
            final Raster tile = coverage.getCachedTile(indexInTileVector);
            if (tile != null) {
                /*
                 * Found a tile, but the sample model may be different because band order may be different.
                 * In any cases, we need to make sure that the raster starts at the expected coordinates.
                 */
                final int x = getTileOrigin(X_DIMENSION);
                final int y = getTileOrigin(Y_DIMENSION);
                final SampleModel model = coverage.model;
                if (model.equals(tile.getSampleModel())) {
                    if (tile.getMinX() == x && tile.getMinY() == y) {
                        return tile;
                    }
                    return tile.createTranslatedChild(x, y);
                }
                /*
                 * If the sample model is not the same (e.g. different bands), it must at least have the same size.
                 * Having a sample model of different size would probably be a bug, but we check anyway for safety.
                 * Note that the tile size is not necessarily equals to the sample model size.
                 */
                final SampleModel sm = tile.getSampleModel();
                if (sm.getWidth() == model.getWidth() && sm.getHeight() == model.getHeight()) {
                    final int width  = tile.getWidth();     // May be smaller than sample model width.
                    final int height = tile.getHeight();    // Idem.
                    /*
                     * It is okay to have a different number of bands if the sample model is
                     * a view created by `SampleModel.createSubsetSampleModel(int[] bands)`.
                     * Bands can also be in a different order and still share the same buffer.
                     */
                    Raster r = Raster.createRaster(model, tile.getDataBuffer(), new Point(x, y));
                    if (r.getWidth() != width || r.getHeight() != height) {
                        r = r.createChild(x, y, width, height, x, y, null);
                    }
                    return r;
                }
            }
            return null;
        }

        /**
         * Stores the given raster in the cache for the current <abbr>AOI</abbr> position.
         * If another raster existed previously in the cache, the old raster will be reused if
         * it has the same size and model, or discarded otherwise. The latter case may happen if
         * {@link #getCachedTile()} determined that a cached raster exists but cannot be reused.
         *
         * @param  tile  the raster to cache.
         * @return the cached raster. Should be the given {@code raster} instance,
         *         but this method check for concurrent caching as a paranoiac check.
         *
         * @see #getCachedTile()
         */
        public Raster cache(final Raster tile) {
            return getCoverage().cacheTile(indexInTileVector, tile);
        }

        /**
         * Creates an initially empty raster for the tile at the current <abbr>AOI</abbr> position.
         * The sample model is {@link #model} and the minimum <var>x</var> and <var>y</var> position
         * are set the the pixel coordinates in the two first dimensions of the <abbr>AOI</abbr>.
         *
         * <p>The raster is <em>not</em> filled with {@link #fillValues}.
         * Filling, if needed, should be done by the caller.</p>
         *
         * @return a newly created, initially empty raster.
         */
        public WritableRaster createRaster() {
            final int x = getTileOrigin(X_DIMENSION);
            final int y = getTileOrigin(Y_DIMENSION);
            return Raster.createWritableRaster(getCoverage().model, new Point(x, y));
        }

        /**
         * Returns the coordinates of the pixels to read <em>inside</em> the tile.
         * The tile upper-left corner is assumed (0,0). Therefore, the lower coordinates computed by this
         * method are usually (0,0) and the rectangle size is usually the tile size, but those values may
         * be different if the enclosing {@link TiledGridCoverage} contains only one (potentially big) tile.
         * The rectangle may also be smaller when reading tiles on the last row or column of the tile matrix.
         *
         * <p>If the {@code subsampled} argument is {@code false}, then this method returns coordinates
         * relative to the tile in the originating {@link TiledGridResource}, i.e. without subsampling.
         * If {@code subsampled} is {@code true}, then this method returns coordinates relative to the
         * {@linkplain #createRaster() raster}, i.e. with {@linkplain #getSubsampling(int) subsampling}.
         * Note that the {@linkplain Raster#getMinX() raster origin} still needs to be added to the
         * {@linkplain Rectangle#getLocation() rectangle location} for obtaining coordinates in the raster space.</p>
         *
         * @param  subsampled  whether to return coordinates with subsampling applied.
         * @return pixel to read inside the tile, or {@code null} if the region is empty.
         * @throws ArithmeticException if the tile coordinates overflow 32 bits integer capacity.
         */
        public Rectangle getRegionInsideTile(final boolean subsampled) {
            final long[] lower = new long[BIDIMENSIONAL];
            final long[] upper = new long[BIDIMENSIONAL];
            if (getRegionInsideTile(lower, upper, null, subsampled)) {
                return new Rectangle(
                        toIntExact(lower[X_DIMENSION]),
                        toIntExact(lower[Y_DIMENSION]),
                        toIntExact(subtractExact(upper[X_DIMENSION], lower[X_DIMENSION])),
                        toIntExact(subtractExact(upper[Y_DIMENSION], lower[Y_DIMENSION])));
            }
            return null;
        }

        /**
         * Returns the coordinates of the pixels to read <em>inside</em> the tile.
         * The tile upper-left corner is assumed (0,0). Therefore, the lower coordinates computed by this
         * method are usually (0,0) and the upper coordinates are usually the tile size, but those values
         * may be different if the enclosing {@link TiledGridCoverage} contains only one (potentially big) tile.
         * In the latter case, the reading process is more like untiled image reading.
         * The rectangle may also be smaller when reading tiles on the last row or column of the tile matrix.
         *
         * <p>The {@link TiledGridCoverage} subsampling is provided for convenience,
         * but is constant for all tiles regardless the subregion to read.
         * The same values can be obtained by {@link #getSubsampling(int)}.</p>
         *
         * <p>If the {@code subsampled} argument is {@code false}, then this method returns coordinates
         * relative to the tile in the originating {@link TiledGridResource}, i.e. without subsampling.
         * If {@code subsampled} is {@code true}, then this method returns coordinates relative to the
         * tile resulting from the read operation, i.e. with {@linkplain #getSubsampling(int) subsampling}.</p>
         *
         * <p>This method is a generalization of {@link #getRegionInsideTile(boolean)} to any number of dimensions.</p>
         *
         * @param  lower        a pre-allocated array where to store relative coordinates of the first pixel.
         * @param  upper        a pre-allocated array where to store relative coordinates after the last pixel.
         * @param  subsampling  a pre-allocated array where to store subsampling, or {@code null} if not needed.
         * @param  subsampled   whether to return coordinates with subsampling applied.
         * @return {@code true} on success, or {@code false} if the tile is empty.
         */
        public boolean getRegionInsideTile(final long[] lower, final long[] upper,
                final int[] subsampling, final boolean subsampled)
        {
            int dimension = Math.min(lower.length, upper.length);
            final TiledGridCoverage coverage = getCoverage();
            if (subsampling != null) {
                System.arraycopy(coverage.subsampling, 0, subsampling, 0, dimension);
            }
            while (--dimension >= 0) {
                final long tileSize  = coverage.getTileSize(dimension);
                final long tileIndex = addExact(coverage.tmcOfFirstTile[dimension], tmcInSubset[dimension]);
                final long tileBase  = multiplyExact(tileIndex, tileSize);
                /*
                 * The `offset` value is usually zero or negative because the tile to read should be inside the AOI,
                 * e.g. at the right of the AOI left border. It may be positive if the `TiledGridCoverage` contains
                 * only one (potentially big) tile, so the tile reading process become a reading of untiled data.
                 */
                long offset = subtractExact(coverage.readExtent.getLow(dimension), tileBase);
                long limit  = Math.min(addExact(offset, coverage.readExtent.getSize(dimension)), tileSize);
                if (offset < 0) {
                    /*
                     * Example: for `tileSize` = 10 pixels and `subsampling` = 3,
                     * the pixels to read are represented by black small squares below:
                     *
                     *  -10          0         10         20         30
                     *    ┼──────────╫──────────┼──────────┼──────────╫
                     *    │▪▫▫▪▫▫▪▫▫▪║▫▫▪▫▫▪▫▫▪▫│▫▪▫▫▪▫▫▪▫▫│▪▫▫▪▫▫▪▫▫▪║
                     *    ┼──────────╫──────────┼──────────┼──────────╫
                     *
                     * If reading the second tile, then `tileBase` = 10 and `offset` = -10.
                     * The first pixel to read in the second tile has a subsampling offset.
                     * We usually try to avoid this situation because it causes a variable
                     * number of white squares in tiles (4,3,3,4 in the above example),
                     * except when there is only 1 tile to read in which case offset is tolerated.
                     */
                    final int s = coverage.getSubsampling(dimension);
                    offset %= s;
                    if (offset != 0) {
                        offset += s;
                    }
                }
                if (offset >= limit) {          // Test for intersection before we adjust the limit.
                    return false;
                }
                if (dimension == X_DIMENSION && coverage.forceTileSize) {
                    limit = tileSize;
                }
                if (subsampled) {
                    final int s = coverage.getSubsampling(dimension);
                    offset /= s;        // Round toward 0 because we should not have negative coordinates.
                    limit = (limit - 1) / s + 1;
                }
                lower[dimension] = offset;
                upper[dimension] = limit;
            }
            return true;
        }
    }




    /**
     * An iterator over the tiles to read. Instances of this class are computed by {@link #render(GridExtent)}
     * and given to {@link #readTiles(TileIterator)}. The latter is the method that subclasses need to override.
     */
    protected final class TileIterator extends AOI {
        /**
         * Total number of tiles in the AOI, from {@link #tileLower} inclusive to {@link #tileUpper} exclusive.
         * This is the length of the array to be returned by {@link #readTiles(TileIterator)}.
         */
        public final int tileCountInQuery;

        /**
         * Indices (relative to enclosing {@code TiledGridCoverage}) of the upper-left tile to read.
         * Tile indices starts at (0, 0, …), not at the indices of the corresponding tile in resource,
         * for avoiding integer overflow.
         */
        private final int[] tileLower;

        /**
         * Indices (relative to enclosing {@code TiledGridCoverage}) after the bottom-right tile to read.
         */
        private final int[] tileUpper;

        /**
         * Pixel coordinates to assign to the upper-left corner of the region to render, with subsampling applied.
         * This is the difference between the region requested by user and the region which will be rendered.
         */
        private final int[] offsetAOI;

        /**
         * Cell coordinates of current iterator position relative to the Area Of Interest specified by user.
         * Those coordinates are in units of the coverage at full resolution.
         * Initial position is {@link #offsetAOI} multiplied by {@link #subsampling}.
         * This array is modified by calls to {@link #next()}.
         */
        private final long[] tileOffsetFull;

        /**
         * Creates a new Area Of Interest for the given tile indices.
         *
         * @param  tileLower  indices (relative to enclosing {@code TiledGridCoverage}) of the upper-left tile to read.
         * @param  tileUpper  indices (relative to enclosing {@code TiledGridCoverage}) after the bottom-right tile to read.
         * @param  offsetAOI  pixel coordinates to assign to the upper-left corner of the subsampled region to render.
         * @param  dimension  number of dimension of the {@code TiledGridCoverage} grid extent.
         */
        TileIterator(final int[] tileLower, final int[] tileUpper, final int[] offsetAOI, final int dimension) {
            super(tileLower.clone());
            this.tileLower = tileLower;
            this.tileUpper = tileUpper;
            this.offsetAOI = offsetAOI;
            tileOffsetFull = new long[offsetAOI.length];
            /*
             * Initialize variables to values for the first tile to read. The loop does arguments validation and
             * converts the `tileLower` coordinates to index in the `tileOffsets` and `tileByteCounts` vectors.
             */
            indexInTileVector = indexOfFirstTile;
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            int tileCountInQuery = 1;
            for (int i=0; i<dimension; i++) {
                final int lower   = tileLower[i];
                final int count   = subtractExact(tileUpper[i], lower);
                indexInTileVector = addExact(indexInTileVector, multiplyExact(tileStrides[i], lower));
                tileCountInQuery  = multiplyExact(tileCountInQuery, count);
                tileOffsetFull[i] = multiplyFull(offsetAOI[i], getSubsampling(i));
                /*
                 * Following is the pixel coordinate after the last pixel in current dimension.
                 * This is not stored; the intent is to get a potential `ArithmeticException`
                 * now instead of in a call to `next()` during iteration. A negative value
                 * would mean that the AOI does not intersect the region requested by user.
                 */
                final long max = addExact(offsetAOI[i], multiplyExact(getTileSize(i), count));
                assert max > Math.max(offsetAOI[i], 0) : max;
            }
            this.tileCountInQuery = tileCountInQuery;
        }

        /**
         * Returns a new {@code TileIterator} instance over a sub-region of this Area Of Interest.
         * The region is specified by tile indices, with (0,0) being the first tile of the enclosing grid coverage.
         * The given region is intersected with the region of this {@code AOI}.
         * The {@code tileLower} and {@code tileUpper} array can have any length;
         * extra indices are ignored and missing indices are inherited from this AOI.
         * This method is independent to the iterator position of this {@code AOI}.
         *
         * @param  tileLower  indices (relative to enclosing {@code TiledGridCoverage}) of the upper-left tile to read.
         * @param  tileUpper  indices (relative to enclosing {@code TiledGridCoverage}) after the bottom-right tile to read.
         * @return a new {@code TileIterator} instance for the specified sub-region.
         */
        public TileIterator subset(final int[] tileLower, final int[] tileUpper) {
            final int[] offset = this.offsetAOI.clone();
            final int[] lower  = this.tileLower.clone();
            for (int i = Math.min(tileLower.length, lower.length); --i >= 0;) {
                final int base = lower[i];
                final int s = tileLower[i];
                if (s > base) {
                    lower[i] = s;
                    // Use of `ceilDiv(…)` is for consistency with `getTileOrigin(int)`.
                    long origin = ceilDiv(multiplyExact(s - base, getTileSize(i)), getSubsampling(i));
                    offset[i] = toIntExact(addExact(offset[i], origin));
                }
            }
            final int[] upper = this.tileUpper.clone();
            for (int i = Math.min(tileUpper.length, upper.length); --i >= 0;) {
                upper[i] = Math.max(lower[i], Math.min(upper[i], tileUpper[i]));
            }
            return new TileIterator(lower, upper, offset, offset.length);
        }

        /**
         * Returns the enclosing coverage.
         */
        @Override
        final TiledGridCoverage getCoverage() {
            return TiledGridCoverage.this;
        }

        /**
         * Returns the extent of the current tile in units of the full coverage resource (without subsampling).
         * This method is a generalization to <var>n</var> dimensions of the rectangle computed by the
         * following code:
         *
         * {@snippet lang="java" :
         *     WritableRaster tile = createRaster();
         *     Rectangle target = tile.getBounds();
         *     Rectangle source = pixelToResourceCoordinates(bounds);
         * }
         *
         * @return extent of this tile in units of the full coverage resource.
         *
         * @see #pixelToResourceCoordinates(Rectangle)
         */
        public final GridExtent getTileExtentInResource() {
            final int dimension = tileOffsetFull.length;
            final var    axes  = new DimensionNameType[dimension];
            final long[] lower = new long[dimension];
            final long[] upper = new long[dimension];
            for (int i=0; i<dimension; i++) {
                lower[i] = pixelToResourceCoordinate(getTileOrigin(i), i);
                upper[i] = addExact(lower[i], getTileSize(i));
                axes [i] = readExtent.getAxisType(i).orElse(null);
            }
            return new GridExtent(axes, lower, upper, false);
        }

        /**
         * Returns the origin to assign to the tile at current iterator position.
         * Note that the subsampling should be a divisor of tile size,
         * otherwise a drift in pixel coordinates will appear.
         * There are two exceptions to this rule:
         *
         * <ul>
         *   <li>If image is untiled (i.e. there is only one tile),
         *       we allow to read a sub-region of the unique tile.</li>
         *   <li>If subsampling is larger than tile size.</li>
         * </ul>
         */
        @Override
        final int getTileOrigin(final int dimension) {
            /*
             * We really need `ceilDiv(…)` below, not `floorDiv(…)`. It makes no difference in the usual
             * case where the subsampling is a divisor of the tile size (the numerator is initialized to
             * a multiple of the denominator, then incremented by another multiple of the denominator).
             * It makes no difference in the untiled case neither because the numerator is not incremented.
             * But if we are in the case where subsampling is larger than tile size, then we want rounding
             * to the next tile. The tile seems "too far", but it will either be discarded at a later step
             * (because of empty intersection with AOI) or compensated by the offset caused by subsampling.
             * At first the index values seem inconsistent, but after we discard the tiles where
             * `getRegionInsideTile(…)` returns `false`, they become consistent.
             */
            return toIntExact(ceilDiv(tileOffsetFull[dimension], getSubsampling(dimension)));
        }

        /**
         * Moves the iterator position to next tile. This method should be invoked in a loop as below:
         *
         * {@snippet lang="java" :
         *     do {
         *         // Process current tile.
         *     } while (domain.next());
         *     }
         *
         * @return {@code true} on success, or {@code false} if the iteration is finished.
         */
        public boolean next() {
            if (++indexInResultArray >= tileCountInQuery) {
                return false;
            }
            /*
             * Iterates over all tiles in the region specified to this method by maintaining 4 indices:
             *
             *   - `indexInResultArray` is the index in the `rasters` array to be returned.
             *   - `indexInTileVector`  is the corresponding index in the `tileOffsets` vector.
             *   - `tmcInSubset[]`      contains the Tile Matrix Coordinates (TMC) relative to this `TiledGridCoverage`.
             *   - `tileOffsetFull[]`   contains the pixel coordinates relative to the user-specified AOI.
             *
             * We do not check for integer overflow in this method because if an overflow is possible,
             * then `ArithmeticException` should have occurred in `TiledGridCoverage` constructor.
             */
            for (int i=0; i<tmcInSubset.length; i++) {
                indexInTileVector += tileStrides[i];
                if (++tmcInSubset[i] < tileUpper[i]) {
                    tileOffsetFull[i] += getTileSize(i);
                    break;
                }
                // Rewind to index for tileLower[i].
                indexInTileVector -= (tmcInSubset[i] - tileLower[i]) * tileStrides[i];
                tmcInSubset   [i]  = tileLower[i];
                tileOffsetFull[i]  = multiplyFull(offsetAOI[i], getSubsampling(i));
            }
            return true;
        }
    }




    /**
     * Snapshot of a {@link TileIterator} position. Those snapshots can be created during an iteration
     * for processing a tile later. For example, a {@link #readTiles(TileIterator)} method implementation
     * may want to create a list of all tiles to load before to start the actual reading process in order
     * to read the tiles in some optimal order, or for combining multiple read operations in a single operation.
     */
    protected static class Snapshot extends AOI {
        /**
         * The source coverage.
         */
        private final TiledGridCoverage coverage;

        /**
         * Pixel coordinates of the upper-left corner of the tile.
         */
        public final int originX, originY;

        /**
         * Stores information about a tile to be loaded.
         *
         * @param iterator  the iterator for which to create a snapshot of its current position.
         */
        public Snapshot(final AOI iterator) {
            super(iterator.tmcInSubset.clone());
            coverage           = iterator.getCoverage();
            indexInResultArray = iterator.indexInResultArray;
            indexInTileVector  = iterator.indexInTileVector;
            originX            = iterator.getTileOrigin(X_DIMENSION);
            originY            = iterator.getTileOrigin(Y_DIMENSION);
        }

        /**
         * Returns the enclosing coverage.
         */
        @Override
        final TiledGridCoverage getCoverage() {
            return coverage;
        }

        /**
         * Returns the origin to assign to the tile at the current iterator position.
         * This is needed by the parent class only for the two first dimensions.
         *
         * @see TileIterator#getTileOrigin(int)
         */
        @Override
        final int getTileOrigin(final int dimension) {
            switch (dimension) {
                case X_DIMENSION: return originX;
                case Y_DIMENSION: return originY;
                default: throw new AssertionError(dimension);
            }
        }
    }

    /**
     * Creates the key to use for caching the tile at given index.
     */
    private TiledGridResource.CacheKey createCacheKey(final int indexInTileVector) {
        return new TiledGridResource.CacheKey(indexInTileVector, includedBands, subsampling, subsamplingOffsets);
    }

    /**
     * Returns a raster in the cache, or {@code null} if none.
     * See {@link AOI#getCachedTile()} for more information.
     */
    private Raster getCachedTile(final int indexInTileVector) {
        return rasters.get(createCacheKey(indexInTileVector));
    }

    /**
     * Caches the given raster. See {@link AOI#cache(Raster)} for more information.
     */
    private Raster cacheTile(final int indexInTileVector, final Raster tile) {
        final TiledGridResource.CacheKey key = createCacheKey(indexInTileVector);
        Raster existing = rasters.put(key, tile);
        /*
         * If a tile already exists, verify if its layout is compatible with the given tile.
         * If yes, we assume that the two tiles have the same content. We do this check as a
         * safety but it should not happen if the caller synchronized the tile read actions.
         */
        if (existing != null
                && existing.getSampleModel().equals(tile.getSampleModel())
                && existing.getWidth()  == tile.getWidth()
                && existing.getHeight() == tile.getHeight())
        {
            // Restore the existing tile in the cache, with its original position.
            if (rasters.replace(key, tile, existing)) {
                final int x = tile.getMinX();
                final int y = tile.getMinY();
                if (existing.getMinX() != x || existing.getMinY() != y) {
                    existing = existing.createTranslatedChild(x, y);
                }
                return existing;
            }
        }
        return tile;
    }

    /**
     * Returns all tiles in the given area of interest. Tile indices are relative to this {@code TiledGridCoverage}:
     * (0,0) is the tile in the upper-left corner of this {@code TiledGridCoverage} (not necessarily the upper-left
     * corner of the image in the {@link TiledGridResource}).
     *
     * The {@link Raster#getMinX()} and {@code getMinY()} coordinates of returned rasters
     * shall start at the given {@code iterator.offsetAOI} values.
     *
     * <p>This method must be thread-safe. It is implementer responsibility to ensure synchronization,
     * for example using {@link TiledGridResource#getSynchronizationLock()}.</p>
     *
     * @param  iterator  an iterator over the tiles that intersect the Area Of Interest specified by user.
     * @return tiles decoded from the {@link TiledGridResource}.
     * @throws IOException if an I/O error occurred.
     * @throws DataStoreException if a logical error occurred.
     * @throws RuntimeException if the Java2D image cannot be created for another reason
     *         (too many exception types to list them all).
     */
    protected abstract Raster[] readTiles(TileIterator iterator) throws IOException, DataStoreException;

    /**
     * Converts raster coordinate from this coverage to {@code TiledGridResource} coordinate space.
     * This method removes the subsampling effect, i.e. returns the coordinates that we would have if this
     * coverage was at full resolution. Such unsampled {@code TiledGridCoverage} uses the same coordinates
     * as the originating {@link TiledGridResource}.
     *
     * <p>This method uses the "pixel" word for simplicity and because this method is used for
     * the two-dimensional case, but "pixel" should be understood as "grid coverage cell".</p>
     *
     * @param  bounds  the rectangle to convert.
     * @return the converted rectangle.
     * @throws ArithmeticException if the coordinate cannot be represented as an integer.
     *
     * @see TileIterator#getTileExtentInResource()
     */
    protected final Rectangle pixelToResourceCoordinates(final Rectangle bounds) {
        return new Rectangle(
                toIntExact(pixelToResourceCoordinate(bounds.x, X_DIMENSION)),
                toIntExact(pixelToResourceCoordinate(bounds.y, Y_DIMENSION)),
                multiplyExact(bounds.width,  subsampling[X_DIMENSION]),
                multiplyExact(bounds.height, subsampling[Y_DIMENSION]));
    }
}
