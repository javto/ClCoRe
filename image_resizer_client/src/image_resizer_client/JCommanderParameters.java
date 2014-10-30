package image_resizer_client;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;

public class JCommanderParameters {
    private static final int TWIDTH = 100;
    private static final int THEIGHT = 100;
    private static final int SWIDTH = 300;
    private static final int SHEIGHT = 300;
    private static final int MWIDTH = 500;
    private static final int MHEIGHT = 500;
    private static final int LWIDTH = 800;
    private static final int LHEIGHT = 800;

    @Parameter(names = "-tw", description = "Width for thumbnail image")
    public Integer twidth = TWIDTH;

    @Parameter(names = "-th", description = "Height for thumbnail image")
    public Integer theight = THEIGHT;

    @Parameter(names = "-mw", description = "Width for medium image")
    public Integer mwidth = MWIDTH;

    @Parameter(names = "-mh", description = "Height for medium image")
    public Integer mheight = MHEIGHT;

    @Parameter(names = "-sw", description = "Width for small image")
    public Integer swidth = SWIDTH;

    @Parameter(names = "-sh", description = "Height for small image")
    public Integer sheight = SHEIGHT;

    @Parameter(names = "-lw", description = "Width for large image")
    public Integer lwidth = LWIDTH;

    @Parameter(names = "-lh", description = "Height for large image")
    public Integer lheight = LHEIGHT;

    @Parameter(names = "-file", description = "Location of original file", converter = FileConverter.class)
    public File file;

}
