package de.uni_freiburg.iems.beatit;

import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 *  This class can handle the segmentation and feature extraction from inertial data
 *  of the wrist. It segments the input data into windows of a fixed duration and extracts
 *  the mean and variance of each input dimension.
 *
 *  It can also be used from the command line, when reading character-separated-values (CSV)
 *  data.
 */
public class SegFeat {

    public static class Builder {
        
        private SegFeat self;

        public Builder() {
            self = new SegFeat();
        }

        /**
         * sets the amount of samples to be consumed before a feature vector is calculated.
         *
         * @param window_length number of samples (calls to write()) that make up a feature vector
         * @return the builder object
         */
        public Builder setWindowSize(int window_length) {
            self.mWindowLength = window_length;
            return this;
        }

        /**
         * sets the number of samples of a single sample vector supplied to the write() function
         *
         * @param sample_size number of values in the sample vector
         * @return the builder object
         */
        public Builder setSampleSize(int sample_size) {
            self.mSampleSize = sample_size;
            return this;
        }

        /**
         * creates the actual SegFeat object.
         *
         * @return SegFeat object.
         */
        public SegFeat build() throws Exception {

            if (self.mWindowLength <= 0)
                throw new Exception("window length must be larger than zero, see setWindowSize()");

            if (self.mSampleSize <= 0)
                throw new Exception("sample size must be larger than zero, see setSampleSize()");

            self.mInputBuffer  = new double[self.mSampleSize][self.mWindowLength];
            self.mLabelBuffer = new String[self.mWindowLength];
            self.mOutputBuffer = new LinkedList<>();
            return self;
        }
    }

    protected int mWindowLength = 100,
                  mSampleSize = -1,
                  mCursor = 0;

    protected LinkedList<FeatureVector> mOutputBuffer;
    protected double[][] mInputBuffer;
    protected String[] mLabelBuffer;

    public void write(String label, double[] array) throws Exception {
        if (array.length != mSampleSize)
            throw new Exception(
                    String.format("sample of wrong size (%d != %d)", array.length, mSampleSize));

        /**
         *  do the segmentation here, i.e. collect enough samples until there are at least
         *  mWindowLength samples collected.
         */
        mLabelBuffer[mCursor] = label;
        for (int i=0; i<array.length; i++)
            mInputBuffer[i][mCursor] = array[i];

        /**
         *  once there are enough samples, calculate mean and variance and put into the
         *  mOutputBuffer.
         */
        if (mCursor == mWindowLength-1) {
            calcFeatures();
            mCursor = 0;
        } else
            mCursor ++;
    }

    /**
     * calculate mean, max, and min for each input dimension. Addtionally extracts the most common
     * label for each sample. Store in the output buffer, that can be accessed by the read()
     * method.
     */
    private void calcFeatures() {
        double[] featureVector =
            Arrays.stream(mInputBuffer)
            .map(
              y -> Arrays.stream(y).summaryStatistics())
            .flatMapToDouble(
              z -> DoubleStream.of(z.getAverage(), z.getMax(), z.getMin()))
            .toArray();

        String mostCommonLabel =
            Arrays.stream(mLabelBuffer)
            .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
            .entrySet()
            .stream()
            .max(Comparator.comparing(Map.Entry::getValue))
            .get()
            .getKey();

        mOutputBuffer.add(new FeatureVector(mostCommonLabel, featureVector));
    }

    public FeatureVector read() {
        return mOutputBuffer.size() > 0 ? mOutputBuffer.pop() : null;
    }

    public class FeatureVector {
        public final String mLabel;
        public final double[] mVector;

        public FeatureVector(String label, double[] vector) {
            mLabel = label;
            mVector = vector;
        }
    }
}
