
package org.syndeticlogic.zold.array;

import static org.junit.Assert.*;
import org.junit.Test;
import org.syndeticlogic.codec.BasicCoder;
import org.syndeticlogic.zold.arrays.MetadataDescriptor;

public class MetadataDescriptorTest {

    @Test
    public void testMetaDescriptor() {
    	BasicCoder coder = new BasicCoder(null);
        MetadataDescriptor metadataDescriptor = new MetadataDescriptor(coder, 73313);
        assertEquals(73313, metadataDescriptor.getAttributes());

        byte[] meta = metadataDescriptor.serialize();
        metadataDescriptor = new MetadataDescriptor(coder, meta);
        assertEquals(73313, metadataDescriptor.getAttributes());        
    }
}
