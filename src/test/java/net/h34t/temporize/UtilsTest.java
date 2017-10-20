package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class UtilsTest {
    @Test
    public void containsDuplicates() throws Exception {
        Assert.assertTrue(Utils.containsDuplicates(Arrays.asList("a", "a")));
    }

    @Test
    public void containsDuplicatesSpecialEmpty() throws Exception {
        Assert.assertFalse(Utils.containsDuplicates(Collections.emptyList()));
    }

    @Test
    public void containsDuplicatesSpecialNull() throws Exception {
        Assert.assertFalse(Utils.containsDuplicates(null));
    }

    @Test
    public void containsDuplicatesOneElement() throws Exception {
        Assert.assertFalse(Utils.containsDuplicates(Collections.singletonList("a")));
    }

    @Test
    public void containsDuplicatesNegative() throws Exception {
        Assert.assertFalse(Utils.containsDuplicates(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void containsDuplicatesNegative2() throws Exception {
        Assert.assertFalse(Utils.containsDuplicates(Arrays.asList("a", "A")));
    }

    @Test
    public void ucFirst() throws Exception {
        Assert.assertEquals("Foo", Utils.ucFirst("foo"));
        Assert.assertEquals("Foo", Utils.ucFirst("Foo"));
        Assert.assertEquals("F", Utils.ucFirst("f"));
        Assert.assertEquals("F", Utils.ucFirst("F"));
        Assert.assertEquals("", Utils.ucFirst(""));
        Assert.assertEquals("", Utils.ucFirst(null));
    }

    @Test
    public void toClassName() throws Exception {
        ucFirst();
    }
}