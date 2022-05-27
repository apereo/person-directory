package org.apereo.services.persondir.support.cache;


/**
 * <p>
 * Builds the checksum and hash code needed to create a
 * <code>{@link HashCodeCacheKey}</code>.
 * </p>
 *
 * @author Omar Irbouh
 * @author Alex Ruiz
 */
public final class HashCodeCalculator {

    private static final int INITIAL_HASH = 17;

    private static final int MULTIPLIER = 37;

    private long checkSum;

    /**
     * Counts the number of times <code>{@link #append(int)}</code> is executed.
     * It is also used to build <code>{@link #checkSum}</code> and
     * <code>{@link #hashCode}</code>.
     */
    private int count;

    /**
     * Hash code to build;
     */
    private int hashCode;

    /**
     * Constructor.
     */
    public HashCodeCalculator() {
        super();
        hashCode = INITIAL_HASH;
    }

    /**
     * Recalculates <code>{@link #checkSum}</code> and
     * <code>{@link #hashCode}</code> using the specified value.
     *
     * @param value
     *          the specified value.
     */
    public void append(int value) {
        count++;
        int valueToAppend = count * value;

        hashCode = MULTIPLIER * hashCode + (valueToAppend ^ (valueToAppend >>> 16));
        checkSum += valueToAppend;
    }

    /**
     * @return the number that ensures that the combination hashCode/checSum is
     *         unique
     */
    public long getCheckSum() {
        return checkSum;
    }

    /**
     * @return the calculated hash code
     */
    public int getHashCode() {
        return hashCode;
    }
}
