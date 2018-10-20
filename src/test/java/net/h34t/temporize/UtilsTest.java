package net.h34t.temporize;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class UtilsTest {
    @Test
    public void containsDuplicates() {
        Assert.assertTrue(Utils.containsDuplicates(Arrays.asList("a", "a")));
    }

    @Test
    public void containsDuplicatesSpecialEmpty() {
        Assert.assertFalse(Utils.containsDuplicates(Collections.emptyList()));
    }

    @Test
    public void containsDuplicatesSpecialNull() {
        Assert.assertFalse(Utils.containsDuplicates(null));
    }

    @Test
    public void containsDuplicatesOneElement() {
        Assert.assertFalse(Utils.containsDuplicates(Collections.singletonList("a")));
    }

    @Test
    public void containsDuplicatesNegative() {
        Assert.assertFalse(Utils.containsDuplicates(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void containsDuplicatesNegative2() {
        Assert.assertFalse(Utils.containsDuplicates(Arrays.asList("a", "A")));
    }

    @Test
    public void ucFirst() {
        Assert.assertEquals("Foo", Utils.ucFirst("foo"));
        Assert.assertEquals("Foo", Utils.ucFirst("Foo"));
        Assert.assertEquals("F", Utils.ucFirst("f"));
        Assert.assertEquals("F", Utils.ucFirst("F"));
        Assert.assertEquals("", Utils.ucFirst(""));
        Assert.assertEquals("", Utils.ucFirst(null));
    }

    @Test
    public void toClassName() {
        ucFirst();
    }

    @Test
    public void lcFirst() {
        Assert.assertEquals("foo", Utils.lcFirst("Foo"));
        Assert.assertEquals("fOO", Utils.lcFirst("FOO"));
        Assert.assertEquals("", Utils.lcFirst(""));
        Assert.assertEquals("", Utils.lcFirst(null));
        Assert.assertEquals("f", Utils.lcFirst("F"));
    }
}