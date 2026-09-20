package assignment;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * This is a basic example of what testing may look like.
 * In your tests, you would want to test different parts of your program in unit tests.
 * In this sample, we are testing the ordering of data within a collection.
 */

public class SampleTest {

    public static final int N = 100000;
    static int[] nums;
    static Random rand;

    // This will run ONCE before all other tests. It can be useful to setup up
    // global variables and anything needed for all of the tests.
    @BeforeAll
    static void setupAll() {
        // initialize nums
        nums = new int[N];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = i;
        }

        // shuffle nums
        rand = new Random(42);
        for (int i = 0; i < nums.length; i++) {
            int swapIdx = rand.nextInt(N);
            int tmp = nums[i];
            nums[i] = nums[swapIdx];
            nums[swapIdx] = tmp;
        }
    }

    // This will run before EACH test.
    @BeforeEach
    void setupEach() {

    }

    // This is a test the java arraylist is stored in the same order it was added to
    // this should pass
    @Test
    void testArrayListOrder() {
        ArrayList<Integer> intlist = new ArrayList<>();
        for (int i = 0; i < nums.length; i++) {
            intlist.add(nums[i]);
        }
        int nums_idx = 0;
        for (int actual : intlist) {
            assertEquals("Java ArrayList is not Ordered!!", nums[nums_idx], actual);
            nums_idx++;
        }
    }

    // This is a test the java hashset is stored in the same order it was added to
    // this should fail
    @Test
    void testHashSetOrder() {
        HashSet<Integer> intset = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            intset.add(nums[i]);
        }
        int nums_idx = 0;
        for (int actual : intset) {
            assertEquals("Java HashSet is not Ordered!!", nums[nums_idx], actual);
            nums_idx++;
        }
    }

}
