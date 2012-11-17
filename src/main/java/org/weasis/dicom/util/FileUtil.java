/*******************************************************************************
 * Copyright (c) 2012 Weasis Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.dicom.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static final int FILE_BUFFER = 4096;

    private FileUtil() {
    }

    public static void safeClose(final Closeable object) {
        if (object != null) {
            try {
                object.close();
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        }
    }

    public static void safeClose(ImageInputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
                stream.close();
            } catch (IOException e) {
                LOGGER.debug(e.getMessage());
            }
        }
    }

    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // DIR: The directory is now empty so delete it
        // FILE: Delete the file
        return dir.delete();
    }

    public static void deleteDirectoryContents(final File dir) {
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
                        if (!f.delete()) {
                            LOGGER.info("Cannot delete {}", f.getPath());
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
            }
        }
    }

    public static void prepareToWriteFile(File file) throws IOException {
        if (!file.exists()) {
            // Check the file that doesn't exist yet.
            // Create a new file. The file is writable if the creation succeeds.
            File outputDir = file.getParentFile();
            // necessary to check exists otherwise mkdirs() is false when dir exists
            if (outputDir != null && !outputDir.exists() && !outputDir.mkdirs()) {
                throw new IOException("Cannot write parent directory of " + file.getPath());
            }
        }
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
            LOGGER.error(e.getMessage());
            return 0;
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outFilename);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
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
        if (inputStream == null || out == null) {
            return 0;
        }
        try {
            byte[] buf = new byte[FILE_BUFFER];
            int offset;
            while ((offset = inputStream.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            return -1;
        } catch (InterruptedIOException e) {
            return e.bytesTransferred;
        } catch (IOException e) {
            LOGGER.error("Error when writing file", e);
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
                LOGGER.error("Error when reading properties: {}", propsFile);
                LOGGER.error(e.getMessage());
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
            } catch (IOException e) {
                LOGGER.error("Error when writing properties: {}", propsFile);
                LOGGER.error(e.getMessage());
            } finally {
                FileUtil.safeClose(fout);
            }
        }
    }

    public static boolean isZipFile(File file) {
        boolean isZip = false;
        if (file != null) {
            byte[] magicDirEnd = { 0x50, 0x4b, 0x03, 0x04 };
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                byte[] buffer = new byte[4];

                if ((inputStream.read(buffer)) == 4) {
                    for (int k = 0; k < magicDirEnd.length; k++) {
                        if (buffer[k] != magicDirEnd[k]) {
                            return false;
                        }
                    }
                    isZip = true;
                }
            } catch (IOException e) {
                LOGGER.error("Error when reading zip file: {}", file);
                LOGGER.error(e.getMessage());
            } finally {
                FileUtil.safeClose(inputStream);
            }
        }
        return isZip;
    }

    public static void unzip(File file, File outputDir) {
        if (file != null && outputDir != null) {

            byte buffer[] = new byte[FILE_BUFFER];

            ZipFile zipFile = null;
            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> e = zipFile.entries();

                while (e.hasMoreElements()) {
                    ZipEntry entry = e.nextElement();

                    File destinationPath = new File(outputDir, entry.getName());
                    if (!destinationPath.getParentFile().mkdirs()) {
                        throw new IOException("Cannot write zip file: " + destinationPath.getAbsolutePath());
                    }

                    if (entry.isDirectory()) {
                        continue;
                    } else {
                        bis = new BufferedInputStream(zipFile.getInputStream(entry));
                        bos = new BufferedOutputStream(new FileOutputStream(destinationPath), FILE_BUFFER);

                        int b;
                        while ((b = bis.read(buffer, 0, FILE_BUFFER)) != -1) {
                            bos.write(buffer, 0, b);
                        }
                        bos.close();
                        bis.close();
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error when unzip file: {}", file, e);
            } finally {
                FileUtil.safeClose(bis);
                FileUtil.safeClose(bos);
                try {
                    if (zipFile != null) {
                        zipFile.close();
                    }
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }
    }
}
