/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.polyswarm.swarmit.apiclient;

import java.io.IOException;
import java.io.InputStream;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * InputStream subclass to Read a file object from an AbstractFile
 */
public class ReadContentInputStream extends InputStream {
    private final Logger LOGGER = Logger.getLogger(ReadContentInputStream.class.getName());
    private final AbstractFile abstractFile;
    private int readIdx;
    
    public ReadContentInputStream(AbstractFile abstractFile) {
        this.abstractFile = abstractFile;
        this.readIdx = 0;
    }

    @Override
    /**
     * Read one byte at a time from the AbstractFile content.
     * 
     * @return next byte of data, or -1 if the end of stream is reached.
     * 
     * @throws IOException if a critical error occurs.
     */
    public int read() throws IOException {
        if (readIdx >= abstractFile.getSize()) {
            return -1;
        }
        byte[] byteBuffer = new byte[1];
        int bytesRead = read(byteBuffer, readIdx++, byteBuffer.length);
        if (bytesRead == -1) {
            return -1;
        }
        return byteBuffer[0];
    }
    
    @Override
    /**
     * Wrapper around abstractFile's read(byte[] buf, long offset, long len).
     * 
     * @return number of bytes read, or -1 on error
     * 
     * @throws IOException if critical error in TSK core.
     */
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            return abstractFile.read(b, off, len);
        } catch (TskCoreException ex) {
            throw new IOException("Failed to read content from AbstractFile.", ex);
        }
    }
}
