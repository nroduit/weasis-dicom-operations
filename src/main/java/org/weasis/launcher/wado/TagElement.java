package org.weasis.launcher.wado;

public class TagElement {

    public enum DICOM_LEVEL {
        Patient, Study, Series, Instance
    };

    public enum TagType {
        String, Text, URI, Sequence, Date, DateTime, Time, Boolean, Integer, IntegerArray, Float, FloatArray, Double,
        DoubleArray, Color;

    };

    public static final TagElement WadoCompressionRate = new TagElement("Wado Compression Rate", TagType.Integer);
    public final static TagElement WadoTransferSyntaxUID = new TagElement("Wado Transfer Syntax UID", TagType.String);

    public final static TagElement TransferSyntaxUID =
        new TagElement(0x00020010, "Transfer Syntax UID", TagType.String);

    public final static TagElement PatientName = new TagElement(0x00100010, "Patient Name", TagType.String);
    public final static TagElement PatientID = new TagElement(0x00100020, "PatientID", TagType.String);
    public final static TagElement PatientBirthDate = new TagElement(0x00100030, "Patient Birth Date", TagType.Date);
    public final static TagElement PatientBirthTime = new TagElement(0x00100032, "Patient Birth Time", TagType.Time);
    public final static TagElement PatientSex = new TagElement(0x00100040, "Patient Sex", TagType.String);

    public final static TagElement StudyInstanceUID = new TagElement(0x0020000D, "Study Instance UID", TagType.String);
    public final static TagElement SeriesInstanceUID =
        new TagElement(0x0020000E, "Series Instance UID", TagType.String);
    public final static TagElement StudyID = new TagElement(0x00200010, "Study ID", TagType.String);
    public final static TagElement InstanceNumber = new TagElement(0x00200013, "Instance Number", TagType.Integer);
    public static final TagElement ImageOrientationPatient =
        new TagElement(0x00200037, "Image Orientation", TagType.DoubleArray);
    public final static TagElement SliceLocation = new TagElement(0x00201041, "Slice Location", TagType.Float);

    public final static TagElement SeriesDescription = new TagElement(0x0008103E, "Series Description", TagType.String);
    public static final TagElement SeriesNumber = new TagElement(0x00200011, "Series Number", TagType.Integer);
    public final static TagElement SOPInstanceUID = new TagElement(0x00080018, "SOP Instance UID", TagType.String);
    public final static TagElement StudyDate = new TagElement(0x00080020, "Study Date", TagType.Date);
    public final static TagElement SeriesDate = new TagElement(0x00080021, "Series Date", TagType.Date);
    public final static TagElement StudyTime = new TagElement(0x00080030, "Study Time", TagType.Time);
    public final static TagElement AcquisitionTime = new TagElement(0x00080032, "Acquisition Time", TagType.Time);
    public final static TagElement AccessionNumber = new TagElement(0x00080050, "Accession Number", TagType.String);
    public final static TagElement Modality = new TagElement(0x00080060, "Modality", TagType.String);
    public final static TagElement ReferringPhysicianName =
        new TagElement(0x00080090, "Referring Physician Name", TagType.String);
    public final static TagElement StudyDescription = new TagElement(0x00081030, "Study Description", TagType.String);

    public static final TagElement PixelData = new TagElement(0x7FE00010, "Pixel Data", TagType.Text);
    public static final TagElement PixelSpacing = new TagElement(0x00280030, "Pixel Spacing", TagType.DoubleArray);
    public static final TagElement WindowWidth = new TagElement(0x00281051, "Window Width", TagType.Float);
    public static final TagElement WindowCenter = new TagElement(0x00281050, "Window Center", TagType.Float);
    public static final TagElement RescaleSlope = new TagElement(0x00281053, "Rescale Slope", TagType.Float);
    public static final TagElement RescaleIntercept = new TagElement(0x00281052, "Rescale Intercept", TagType.Float);
    public static final TagElement SmallestImagePixelValue =
        new TagElement(0x00280106, "Smallest ImagePixel Value", TagType.Float);
    public static final TagElement LargestImagePixelValue =
        new TagElement(0x00200013, "Largest Image PixelValue", TagType.Float);
    public static final TagElement PixelPaddingValue =
        new TagElement(0x00280120, "Pixel Padding Value", TagType.Integer);
    public static final TagElement PixelPaddingRangeLimit =
        new TagElement(0x00280121, "Pixel Padding Range Limit", TagType.Integer);
    public static final TagElement SamplesPerPixel = new TagElement(0x00280107, "Samples Per Pixel", TagType.Integer);
    public static final TagElement MonoChrome = new TagElement("MonoChrome", TagType.Boolean);
    public static final TagElement PhotometricInterpretation =
        new TagElement(0x00280004, "Photometric Interpretation", TagType.String);

    protected final int id;
    protected final String name;
    protected final TagType type;

    public TagElement(int id, String name) {
        this(id, name, null);
    }

    public TagElement(int id, String name, TagType type) {
        this.id = id;
        this.name = name;
        this.type = type == null ? TagType.String : type;
    }

    public TagElement(String name) {
        this(-1, name, null);
    }

    public TagElement(String name, TagType type) {
        this(-1, name, type);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTagName() {
        return name.replaceAll(" ", "");
    }

    public TagType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }
}
