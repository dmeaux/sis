// Only math functions working on floating point numbers.
import static java.lang.Math.E;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cbrt;
import static java.lang.Math.ceil;
import static java.lang.Math.copySign;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.exp;
import static java.lang.Math.expm1;
import static java.lang.Math.floor;
import static java.lang.Math.fma;
import static java.lang.Math.hypot;
import static java.lang.Math.log;
import static java.lang.Math.log10;
import static java.lang.Math.log1p;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.nextAfter;
import static java.lang.Math.nextDown;
import static java.lang.Math.nextUp;
import static java.lang.Math.rint;
import static java.lang.Math.round;
import static java.lang.Math.scalb;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sinh;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.tanh;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.lang.Math.ulp;

// Arbitrary precision arithmetic.
import java.math.*;

// Main I/O objects.
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.LinkPermission;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.net.URL;
import java.net.URI;

// Most interesting utilities, excluding calendar.
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.Deque;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.PrimitiveIterator;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.*;
import java.util.stream.*;
import java.util.function.*;

// Time objects preferred to calendar.
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

// Unit of measurement.
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;

// GeoAPI main objects.
import org.opengis.util.CodeList
import org.opengis.util.FactoryException
import org.opengis.util.GenericName
import org.opengis.util.InternationalString
import org.opengis.metadata.Identifier;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.metadata.extent.GeographicExtent;
import org.opengis.metadata.extent.SpatialTemporalExtent;
import org.opengis.metadata.extent.TemporalExtent;
import org.opengis.metadata.extent.VerticalExtent;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.quality.DataQuality;
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeneralDerivedCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.RangeMeaning;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.ConcatenatedOperation;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.TransformException;

// Apache SIS.
import org.apache.sis.math.Fraction;
import org.apache.sis.measure.Angle;
import org.apache.sis.measure.Latitude;
import org.apache.sis.measure.Longitude;
import org.apache.sis.measure.MeasurementRange;
import org.apache.sis.measure.NumberRange;
import org.apache.sis.measure.Quantities;
import org.apache.sis.measure.Range;
import org.apache.sis.measure.Units;
import org.apache.sis.util.collection.TableColumn;
import org.apache.sis.util.collection.TreeTable;
import org.apache.sis.util.iso.Names;
import org.apache.sis.xml.NilObject;
import org.apache.sis.xml.NilReason;
import org.apache.sis.xml.XML;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.geometry.Envelopes;
import org.apache.sis.referencing.CommonCRS;
import org.apache.sis.referencing.CRS;
import org.apache.sis.referencing.GeodeticCalculator;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.referencing.crs.AbstractCRS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.operation.matrix.Matrices;
import org.apache.sis.referencing.operation.transform.MathTransforms;
import org.apache.sis.feature.builder.AssociationRoleBuilder;
import org.apache.sis.feature.builder.AttributeRole;
import org.apache.sis.feature.builder.AttributeTypeBuilder;
import org.apache.sis.feature.builder.CharacteristicTypeBuilder;
import org.apache.sis.feature.builder.FeatureTypeBuilder;
import org.apache.sis.feature.builder.PropertyTypeBuilder;
import org.apache.sis.feature.builder.TypeBuilder;
import org.apache.sis.filter.DefaultFilterFactory;
import org.apache.sis.coverage.BandedCoverage;
import org.apache.sis.coverage.Category;
import org.apache.sis.coverage.CoverageCombiner;
import org.apache.sis.coverage.RegionOfInterest;
import org.apache.sis.coverage.SampleDimension;
import org.apache.sis.coverage.grid.GridClippingMode;
import org.apache.sis.coverage.grid.GridCoverage;
import org.apache.sis.coverage.grid.GridCoverage2D;
import org.apache.sis.coverage.grid.GridCoverageBuilder;
import org.apache.sis.coverage.grid.GridCoverageProcessor;
import org.apache.sis.coverage.grid.GridExtent;
import org.apache.sis.coverage.grid.GridGeometry;
import org.apache.sis.coverage.grid.GridOrientation;
import org.apache.sis.coverage.grid.GridRoundingMode;
import org.apache.sis.storage.Aggregate;
import org.apache.sis.storage.CoverageQuery;
import org.apache.sis.storage.DataSet;
import org.apache.sis.storage.DataStore;
import org.apache.sis.storage.DataStores;
import org.apache.sis.storage.FeatureQuery;
import org.apache.sis.storage.FeatureSet;
import org.apache.sis.storage.GridCoverageResource;
import org.apache.sis.storage.Query;
import org.apache.sis.storage.RasterLoadingStrategy;
import org.apache.sis.storage.Resource;
import org.apache.sis.storage.StorageConnector;
import org.apache.sis.storage.WritableAggregate;
import org.apache.sis.storage.WritableFeatureSet;
import org.apache.sis.storage.WritableGridCoverageResource;
import org.apache.sis.console.SIS;
import static org.apache.sis.console.SIS.print;

// Initialization
org.apache.sis.console.Command.configureLogging()
