package org.weasis.dicom.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import javax.imageio.stream.ImageInputStream;

public class FileUtil {

    public static void safeClose(final Closeable object) {
        try {
            if (object != null) {
                object.close();
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    public static void safeClose(ImageInputStream stream) {
        try {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    public static final void deleteDirectoryContents(final File dir) {
        if ((dir == null) || !dir.isDirectory()) {
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryContents(f);
                } else {
                    try {
                        f.delete();
                    } catch (Exception e) {
                        // Do nothing, wait next start to delete it
                    }
                }
            }
        }
    }

    public static boolean isWriteable(File file) {
        if (file.exists()) {
            // Check the existing file.
            if (!file.canWrite()) {
                return false;
            }
        } else {
            // Check the file that doesn't exist yet.
            // Create a new file. The file is writeable if
            // the creation succeeds.
            try {
                String parentDir = file.getParent();
                if (parentDir != null) {
                    File outputDir = new File(file.getParent());
                    if (outputDir.exists() == false) {
                        // Output directory doesn't exist, so create it.
                        outputDir.mkdirs();
                    } else {
                        if (outputDir.isDirectory() == false) {
                            // File, which have a same name as the output directory, exists.
                            // Create output directory.
                            outputDir.mkdirs();
                        }
                    }
                }

                file.createNewFile();
            } catch (IOException ioe) {
                return false;
            }
        }
        return true;
    }

    public static String nameWithoutExtension(String fn) {
        if (fn == null) {
            return null;
        }
        int i = fn.lastIndexOf('.');
        if (i > 0) {
            return fn.substring(0, i);
        }
        return fn;
    }

    public static String getExtension(String fn) {
        if (fn == null) {
            return ""; //$NON-NLS-1$
        }
        int i = fn.lastIndexOf('.');
        if (i > 0) {
            return fn.substring(i);
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * @param inputStream
     * @param out
     * @return bytes transferred. O = error, -1 = all bytes has been transferred, other = bytes transferred before
     *         interruption
     */
    public static int writeFile(URL url, File outFilename) {
        InputStream input;
        try {
            input = url.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return writeFile(input, outputStream);
    }

    /**
     * @param inputStream
     * @param out
     * @return bytes transferred. O = error, -1 = all bytes has been transferred, other = bytes transferred before
     *         interruption
     */
    public static int writeFile(InputStream inputStream, OutputStream out) {
        if (inputStream == null && out == null) {
            return 0;
        }
        try {
            byte[] buf = new byte[4096];
            int offset;
            while ((offset = inputStream.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            return -1;
        } catch (InterruptedIOException e) {
            return e.bytesTransferred;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(out);
        }
    }

    public static Properties readProperties(File propsFile, Properties props) {
        Properties p = props == null ? new Properties() : props;
        if (propsFile != null && propsFile.canRead()) {
            FileInputStream fileStream = null;
            try {
                fileStream = new FileInputStream(propsFile);
                p.load(fileStream);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtil.safeClose(fileStream);
            }
        }
        return p;
    }

    public static void storeProperties(File propsFile, Properties props, String comments) {
        if (props != null && propsFile != null) {
            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(propsFile);
                props.store(fout, comments);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileUtil.safeClose(fout);
            }
        }
    }
}
