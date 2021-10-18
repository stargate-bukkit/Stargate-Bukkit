package net.knarcraft.stargate.container;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RelativeBlockVectorTest {

    @Test
    public void addToVectorTest() {
        int right = 5;
        int depth = 5;
        int distance = 3;

        RelativeBlockVector relativeBlockVector = new RelativeBlockVector(right, depth, distance);

        for (int i = 0; i < 1000; i++) {
            int randomValue = getRandomNumber();
            RelativeBlockVector newVector = relativeBlockVector.addToVector(RelativeBlockVector.Property.RIGHT, randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right + randomValue, depth, distance), newVector);

            newVector = relativeBlockVector.addToVector(RelativeBlockVector.Property.DISTANCE, randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right, depth, distance + randomValue), newVector);

            newVector = relativeBlockVector.addToVector(RelativeBlockVector.Property.DEPTH, randomValue);
            Assertions.assertEquals(new RelativeBlockVector(right, depth + randomValue, distance), newVector);
        }
    }

    @Test
    public void invertTest() {
        for (int i = 0; i < 1000; i++) {
            int randomNumber1 = getRandomNumber();
            int randomNumber2 = getRandomNumber();
            int randomNumber3 = getRandomNumber();

            RelativeBlockVector relativeBlockVector = new RelativeBlockVector(randomNumber1, randomNumber2,
                    randomNumber3);
            RelativeBlockVector invertedBlockVector = new RelativeBlockVector(-randomNumber1, -randomNumber2,
                    -randomNumber3);
            Assertions.assertEquals(invertedBlockVector, relativeBlockVector.invert());
        }
    }

    /**
     * Gets a random number between -500 and 500
     *
     * @return <p>A random number between -500 and 500</p>
     */
    private int getRandomNumber() {
        return (int) ((Math.random() - 0.5) * 1000);
    }

}
