import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.arraycopy;

public class SortThread extends Thread {

    public int[] input;
    public int size;
    public int[] result;
    public int resultIndex;
    static AtomicInteger maxThreadCount;

    public SortThread(int[] input, int size, int[] result, int resultIndex) {
        this.input = input;
        this.size = size;
        this.result = result;
        this.resultIndex = resultIndex;
    }

    public SortThread(int[] input, int size, int threadCount) {
        this.input = input;
        this.size = size;
        this.result = input;
        resultIndex = 0;
        maxThreadCount = new AtomicInteger(threadCount);
    }

    @Override
    public void run() {
        try {
            sort(input, size, result, resultIndex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sort(int[] input, int size, int[] result, int resultIndex) throws InterruptedException {
        if (size <= 0) return;
        if (size == 1) {
            result[resultIndex] = input[0];
            return;
        }

        int pivot;
        int[] less, equals, greater;
        int lessI, equalsI, greaterI;

        int counter = 0;
        double ratio;

        int MIN_FORK_ARR_SIZE = 100_000;
        int RETRY_RATION_MAX = 5;
        int RETRY_MAX_COUNT = 5;
        int RETRY_RATION_MIN = 2;

        do {
            ++counter;
            pivot = input[new Random().nextInt(size)];
            less = new int[size];
            equals = new int[size];
            greater = new int[size];
            lessI = 0;
            equalsI = 0;
            greaterI = 0;
            for (int k = 0; k < size; ++k) {
                int i = input[k];
                if (i < pivot) {
                    less[lessI++] = i;
                } else if (i == pivot) {
                    equals[equalsI++] = i;
                } else {
                    greater[greaterI++] = i;
                }
            }
            ratio = ((double) Math.max(lessI, greaterI)) / ((double) Math.min(lessI, greaterI));
        } while (size > MIN_FORK_ARR_SIZE && counter < RETRY_MAX_COUNT &&
                ratio > RETRY_RATION_MIN && ratio < RETRY_RATION_MAX);

        if (size > MIN_FORK_ARR_SIZE && maxThreadCount.get() > 1 && maxThreadCount.decrementAndGet() > 0) {
            var t = new SortThread(less, lessI, result, resultIndex);
            t.start();
            sort(greater, greaterI, result, resultIndex + lessI + equalsI);
            if (equalsI >= 0) arraycopy(equals, 0, result, resultIndex + lessI, equalsI);
            t.join();
            maxThreadCount.incrementAndGet();
        } else {
            sort(less, lessI, result, resultIndex);
            sort(greater, greaterI, result, resultIndex + lessI + equalsI);
            if (equalsI >= 0) arraycopy(equals, 0, result, resultIndex + lessI, equalsI);
        }
    }
}
